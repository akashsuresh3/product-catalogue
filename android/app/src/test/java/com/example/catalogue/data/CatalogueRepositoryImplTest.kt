package com.example.catalogue.data

import com.example.catalogue.data.remote.CatalogueApi
import com.example.catalogue.data.repository.CatalogueRepositoryImpl
import com.example.catalogue.domain.model.CatalogueException
import com.example.catalogue.domain.model.Enquiry
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Exercises the real Retrofit + serialization + error mapping against a local HTTP server. */
class CatalogueRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: CatalogueRepositoryImpl
    private val json = Json { ignoreUnknownKeys = true }

    @Before fun setUp() {
        server = MockWebServer().also { it.start() }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/api/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CatalogueApi::class.java)
        repo = CatalogueRepositoryImpl(api, json)
    }

    @After fun tearDown() = runCatching { server.shutdown() }.let { }

    private fun productJson(id: Int) =
        """{"id":$id,"title":"T$id","description":"D","price":12.5,"category":"jewelery",""" +
            """"imageUrl":"https://img/$id.png","rating":{"rate":4.6,"count":120},"extra":"ignored"}"""

    @Test fun `home maps DTOs to domain models and ignores unknown fields`() = runTest {
        server.enqueue(MockResponse().setBody("""{"featured":[${productJson(1)}]}"""))

        val result = repo.getFeatured().getOrThrow()

        assertEquals(1, result.size)
        with(result.first()) {
            assertEquals(1, id)
            assertEquals("https://img/1.png", imageUrl)
            assertEquals(4.6, rating, 0.0)
            assertEquals(120, ratingCount)
        }
        assertEquals("/api/home", server.takeRequest().path)
    }

    @Test fun `category names are URL encoded in the path`() = runTest {
        server.enqueue(MockResponse().setBody("""{"topRated":[],"others":[]}"""))

        repo.getCategory("men's clothing")

        assertTrue(server.takeRequest().path!!.endsWith("%20clothing"))
    }

    @Test fun `search sends the query parameter`() = runTest {
        server.enqueue(MockResponse().setBody("""{"products":[${productJson(2)}]}"""))

        val result = repo.search("  gold ring ").getOrThrow()

        assertEquals(2, result.first().id)
        assertEquals("/api/products?q=gold%20ring", server.takeRequest().path)
    }

    @Test fun `404 maps to NotFound`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val failure = repo.getProduct(99).exceptionOrNull()

        assertTrue(failure is CatalogueException.NotFound)
    }

    @Test fun `5xx maps to Server with the status code`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))

        val failure = repo.getFeatured().exceptionOrNull() as CatalogueException.Server

        assertEquals(503, failure.code)
    }

    @Test fun `malformed payload maps to Unknown rather than crashing`() = runTest {
        server.enqueue(MockResponse().setBody("""{"featured":"nope"}"""))

        assertTrue(repo.getFeatured().exceptionOrNull() is CatalogueException.Unknown)
    }

    @Test fun `unreachable server maps to NetworkUnavailable`() = runTest {
        server.shutdown()

        assertTrue(repo.getFeatured().exceptionOrNull() is CatalogueException.NetworkUnavailable)
    }

    @Test fun `enquiry success posts trimmed values and omits an empty phone`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201))

        val result = repo.submitEnquiry(Enquiry(4, " Asha ", " asha@example.com ", " ", " Hi "))

        assertTrue(result.isSuccess)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        val body = request.body.readUtf8()
        assertTrue(body.contains(""""productId":4"""))
        assertTrue(body.contains(""""name":"Asha""""))
        assertFalse(body.contains("phone"))
    }

    @Test fun `enquiry 400 maps field errors`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"errors":{"email":"Invalid email"}}"""))

        val failure = repo.submitEnquiry(Enquiry(4, "A", "bad", null, "Hi")).exceptionOrNull()

        assertEquals(mapOf("email" to "Invalid email"), (failure as CatalogueException.Validation).fieldErrors)
    }

    @Test fun `enquiry 400 with an unreadable body still maps to Validation`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("<html>oops</html>"))

        val failure = repo.submitEnquiry(Enquiry(4, "A", "a@b.co", null, "Hi")).exceptionOrNull()

        assertTrue((failure as CatalogueException.Validation).fieldErrors.isEmpty())
    }
}
