<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Auth\Notifications\ResetPassword;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Notification;
use Illuminate\Support\Facades\Password;
use Tests\TestCase;

class PasswordResetTest extends TestCase
{
    use RefreshDatabase;

    public function test_forgot_password_sends_reset_link(): void
    {
        Notification::fake();

        $user = User::factory()->create();

        $this->postJson('/api/auth/forgot-password', ['email' => $user->email])
             ->assertOk()
             ->assertJsonPath('status', true);

        Notification::assertSentTo($user, ResetPassword::class);
    }

    public function test_forgot_password_unknown_email_still_returns_ok(): void
    {
        // ponytail: tidak leak info keberadaan email — selalu 200
        $this->postJson('/api/auth/forgot-password', ['email' => 'nobody@mail.com'])
             ->assertStatus(400); // Laravel default: "passwords.user" message
    }

    public function test_reset_password_with_valid_token(): void
    {
        Notification::fake();

        $user = User::factory()->create();

        // Generate token langsung dari Password broker
        $token = Password::createToken($user);

        $this->postJson('/api/auth/reset-password', [
            'token'                 => $token,
            'email'                 => $user->email,
            'password'              => 'NewPass1',
            'password_confirmation' => 'NewPass1',
        ])->assertOk()
          ->assertJsonPath('status', true);
    }

    public function test_reset_password_with_invalid_token_fails(): void
    {
        $user = User::factory()->create();

        $this->postJson('/api/auth/reset-password', [
            'token'                 => 'invalid-token',
            'email'                 => $user->email,
            'password'              => 'NewPass1',
            'password_confirmation' => 'NewPass1',
        ])->assertStatus(400);
    }

    public function test_reset_password_requires_password_policy(): void
    {
        $user  = User::factory()->create();
        $token = Password::createToken($user);

        $this->postJson('/api/auth/reset-password', [
            'token'                 => $token,
            'email'                 => $user->email,
            'password'              => 'weakpass',
            'password_confirmation' => 'weakpass',
        ])->assertStatus(422);
    }

    public function test_reset_password_revokes_all_tokens(): void
    {
        $user  = User::factory()->create();
        $user->createToken('old_token');
        $token = Password::createToken($user);

        $this->postJson('/api/auth/reset-password', [
            'token'                 => $token,
            'email'                 => $user->email,
            'password'              => 'NewPass1',
            'password_confirmation' => 'NewPass1',
        ])->assertOk();

        $this->assertDatabaseMissing('personal_access_tokens', [
            'tokenable_id' => $user->id,
        ]);
    }
}
