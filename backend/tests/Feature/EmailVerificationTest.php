<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Auth\Notifications\VerifyEmail;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Facades\URL;
use Tests\TestCase;

class EmailVerificationTest extends TestCase
{
    use RefreshDatabase;

    public function test_send_verification_notification(): void
    {
        Notification::fake();

        $user = User::factory()->unverified()->create();

        $this->actingAs($user)
             ->postJson('/api/auth/email/verification-notification')
             ->assertOk()
             ->assertJsonPath('status', true);

        Notification::assertSentTo($user, VerifyEmail::class);
    }

    public function test_send_verification_fails_if_already_verified(): void
    {
        $user = User::factory()->create(); // factory default: email_verified_at set

        $this->actingAs($user)
             ->postJson('/api/auth/email/verification-notification')
             ->assertStatus(400);
    }

    public function test_verify_email_with_valid_signed_url(): void
    {
        $user = User::factory()->unverified()->create();

        $url = URL::temporarySignedRoute(
            'verification.verify',
            now()->addMinutes(60),
            ['id' => $user->id, 'hash' => sha1($user->email)]
        );

        // Extract path dari URL
        $path = parse_url($url, PHP_URL_PATH) . '?' . parse_url($url, PHP_URL_QUERY);

        $this->actingAs($user)
             ->getJson($path)
             ->assertOk()
             ->assertJsonPath('status', true);

        $this->assertNotNull($user->fresh()->email_verified_at);
    }

    public function test_verify_email_with_invalid_hash_fails(): void
    {
        $user = User::factory()->unverified()->create();

        $url = URL::temporarySignedRoute(
            'verification.verify',
            now()->addMinutes(60),
            ['id' => $user->id, 'hash' => 'wronghash']
        );

        $path = parse_url($url, PHP_URL_PATH) . '?' . parse_url($url, PHP_URL_QUERY);

        $this->actingAs($user)
             ->getJson($path)
             ->assertStatus(403);
    }
}
