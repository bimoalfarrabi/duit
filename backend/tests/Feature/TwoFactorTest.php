<?php

namespace Tests\Feature;

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use PragmaRX\Google2FA\Google2FA;
use Tests\TestCase;

class TwoFactorTest extends TestCase
{
    use RefreshDatabase;

    private Google2FA $google2fa;

    protected function setUp(): void
    {
        parent::setUp();
        $this->google2fa = new Google2FA();
    }

    public function test_enable_2fa_returns_secret_and_qr_url(): void
    {
        $user = User::factory()->create();

        $response = $this->actingAs($user)
                         ->postJson('/api/auth/two-factor-authentication')
                         ->assertOk()
                         ->assertJsonStructure(['data' => ['secret', 'qr_url']]);

        $this->assertNotNull($user->fresh()->two_factor_secret);
        $this->assertNull($user->fresh()->two_factor_confirmed_at);
    }

    public function test_confirm_2fa_with_valid_code(): void
    {
        $user   = User::factory()->create();
        $secret = $this->google2fa->generateSecretKey();
        $user->update(['two_factor_secret' => $secret]);

        $code = $this->google2fa->getCurrentOtp($secret);

        $this->actingAs($user)
             ->postJson('/api/auth/two-factor-authentication/confirm', ['code' => $code])
             ->assertOk()
             ->assertJsonPath('status', true);

        $this->assertNotNull($user->fresh()->two_factor_confirmed_at);
    }

    public function test_confirm_2fa_with_invalid_code_fails(): void
    {
        $user   = User::factory()->create();
        $secret = $this->google2fa->generateSecretKey();
        $user->update(['two_factor_secret' => $secret]);

        $this->actingAs($user)
             ->postJson('/api/auth/two-factor-authentication/confirm', ['code' => '000000'])
             ->assertStatus(422);
    }

    public function test_login_with_2fa_active_returns_temp_token(): void
    {
        $secret = $this->google2fa->generateSecretKey();
        $user   = User::factory()->create([
            'password'                => bcrypt('Secret1'),
            'two_factor_secret'       => $secret,
            'two_factor_confirmed_at' => now(),
        ]);

        $response = $this->postJson('/api/auth/login', [
            'email'    => $user->email,
            'password' => 'Secret1',
        ])->assertOk()
          ->assertJsonPath('data.requires_2fa', true)
          ->assertJsonStructure(['data' => ['temp_token']]);
    }

    public function test_two_factor_challenge_with_valid_code_returns_auth_token(): void
    {
        $secret = $this->google2fa->generateSecretKey();
        $user   = User::factory()->create([
            'password'                => bcrypt('Secret1'),
            'two_factor_secret'       => $secret,
            'two_factor_confirmed_at' => now(),
        ]);

        $tempToken = $user->createToken('2fa-temp', ['2fa-challenge'], now()->addMinutes(5))->plainTextToken;
        $code      = $this->google2fa->getCurrentOtp($secret);

        $this->postJson('/api/auth/two-factor-challenge', [
            'temp_token' => $tempToken,
            'code'       => $code,
        ])->assertOk()
          ->assertJsonStructure(['data' => ['token']]);
    }

    public function test_two_factor_challenge_with_invalid_code_fails(): void
    {
        $secret = $this->google2fa->generateSecretKey();
        $user   = User::factory()->create([
            'two_factor_secret'       => $secret,
            'two_factor_confirmed_at' => now(),
        ]);

        $tempToken = $user->createToken('2fa-temp', ['2fa-challenge'], now()->addMinutes(5))->plainTextToken;

        $this->postJson('/api/auth/two-factor-challenge', [
            'temp_token' => $tempToken,
            'code'       => '000000',
        ])->assertStatus(422);
    }

    public function test_disable_2fa_with_valid_code(): void
    {
        $secret = $this->google2fa->generateSecretKey();
        $user   = User::factory()->create([
            'two_factor_secret'       => $secret,
            'two_factor_confirmed_at' => now(),
        ]);

        $code = $this->google2fa->getCurrentOtp($secret);

        $this->actingAs($user)
             ->deleteJson('/api/auth/two-factor-authentication', ['code' => $code])
             ->assertOk();

        $this->assertNull($user->fresh()->two_factor_secret);
        $this->assertNull($user->fresh()->two_factor_confirmed_at);
    }
}
