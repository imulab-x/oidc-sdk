package io.imulab.astrea.sdk.oidc.claim

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.BehaviorSpec
import org.jose4j.jwk.RsaJwkGenerator
import org.jose4j.jwt.consumer.JwtConsumerBuilder

class ClaimsTest : BehaviorSpec({

    Given("an example token with claims from Open ID Connect Core 1.0 specification") {
        val example = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImsyYmRjIn0.ew0KICJpc3MiOiAiczZCaGRSa3F0MyIsDQogImF1ZCI6ICJodHRwczovL3NlcnZlci5leGFtcGxlLmNvbSIsDQogInJlc3BvbnNlX3R5cGUiOiAiY29kZSBpZF90b2tlbiIsDQogImNsaWVudF9pZCI6ICJzNkJoZFJrcXQzIiwNCiAicmVkaXJlY3RfdXJpIjogImh0dHBzOi8vY2xpZW50LmV4YW1wbGUub3JnL2NiIiwNCiAic2NvcGUiOiAib3BlbmlkIiwNCiAic3RhdGUiOiAiYWYwaWZqc2xka2oiLA0KICJub25jZSI6ICJuLTBTNl9XekEyTWoiLA0KICJtYXhfYWdlIjogODY0MDAsDQogImNsYWltcyI6IA0KICB7DQogICAidXNlcmluZm8iOiANCiAgICB7DQogICAgICJnaXZlbl9uYW1lIjogeyJlc3NlbnRpYWwiOiB0cnVlfSwNCiAgICAgIm5pY2tuYW1lIjogbnVsbCwNCiAgICAgImVtYWlsIjogeyJlc3NlbnRpYWwiOiB0cnVlfSwNCiAgICAgImVtYWlsX3ZlcmlmaWVkIjogeyJlc3NlbnRpYWwiOiB0cnVlfSwNCiAgICAgInBpY3R1cmUiOiBudWxsDQogICAgfSwNCiAgICJpZF90b2tlbiI6IA0KICAgIHsNCiAgICAgImdlbmRlciI6IG51bGwsDQogICAgICJiaXJ0aGRhdGUiOiB7ImVzc2VudGlhbCI6IHRydWV9LA0KICAgICAiYWNyIjogeyJ2YWx1ZXMiOiBbInVybjptYWNlOmluY29tbW9uOmlhcDpzaWx2ZXIiXX0NCiAgICB9DQogIH0NCn0.nwwnNsk1-ZkbmnvsF6zTHm8CHERFMGQPhos-EJcaH4Hh-sMgk8ePrGhw_trPYs8KQxsn6R9Emo_wHwajyFKzuMXZFSZ3p6Mb8dkxtVyjoy2GIzvuJT_u7PkY2t8QU9hjBcHs68PkgjDVTrG1uRTx0GxFbuPbj96tVuj11pTnmFCUR6IEOXKYr7iGOCRB3btfJhM0_AKQUfqKnRlrRscc8Kol-cSLWoYE9l5QqholImzjT_cMnNIznW9E7CDyWXTsO70xnB4SkG6pXfLSjLLlxmPGiyon_-Te111V8uE83IlzCYIb_NMXvtTIVc1jpspnTSD7xMbpL-2QgwUsAlMGzw"

        When("decode") {
            val claims = Claims(
                    JwtConsumerBuilder().also {
                        it.setSkipAllValidators()
                        it.setDisableRequireSignature()
                        it.setSkipSignatureVerification()
                        it.setVerificationKey(RsaJwkGenerator.generateJwk(2048).getRsaPublicKey())
                    }.build().processToClaims(example).getClaimValue("claims", LinkedHashMap<String, Any>().javaClass)
            )

            Then("should have given_name claim") {
                claims.getClaim("given_name") shouldNotBe null
                claims.getAllClaims().find { it.name == "given_name" } shouldNotBe null
            }

            then("should have nickname claim") {
                claims.getClaim("nickname") shouldNotBe null
                claims.getAllClaims().find { it.name == "nickname" } shouldNotBe null
            }

            then("should have email claim") {
                claims.getClaim("email") shouldNotBe null
                claims.getAllClaims().find { it.name == "email" } shouldNotBe null
            }

            then("should have email_verified claim") {
                claims.getClaim("email_verified") shouldNotBe null
                claims.getAllClaims().find { it.name == "email_verified" } shouldNotBe null
            }

            then("should have picture claim") {
                claims.getClaim("picture") shouldNotBe null
                claims.getAllClaims().find { it.name == "picture" } shouldNotBe null
            }

            then("should have gender claim") {
                claims.getClaim("gender") shouldNotBe null
                claims.getAllClaims().find { it.name == "gender" } shouldNotBe null
            }

            then("should have birthdate claim") {
                claims.getClaim("birthdate") shouldNotBe null
                claims.getAllClaims().find { it.name == "birthdate" } shouldNotBe null
            }

            then("should have acr claim") {
                claims.getClaim("acr") shouldNotBe null
                claims.getAllClaims().find { it.name == "acr" } shouldNotBe null
            }
        }
    }

    Given("an empty claims") {
        val claims = Claims()
        check(claims.isEmpty())

        When("require auth_time is set") {
            claims.requireAuthTime()

            Then("auth_time should be part of the claims") {
                claims.isNotEmpty() shouldBe true
                claims.getClaim("auth_time") shouldNotBe null
            }
        }
    }

    Given("a list of claims") {
        val claimList = listOf(
                Claim(name = "foo", essential = false, source = "id_token")
        )

        When("used to construct claims") {
            val claims = Claims(claimList)

            Then("claims should contain items in the list") {
                claims.getClaim("foo") shouldNotBe null
            }
        }
    }
})