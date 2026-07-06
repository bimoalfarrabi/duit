<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use PragmaRX\Google2FA\Google2FA;

class TwoFactorController extends Controller
{
    public function __construct(private readonly Google2FA $google2fa) {}

    public function enable(Request $request): JsonResponse
    {
        $user = $request->user();

        if ($user->two_factor_confirmed_at) {
            return $this->error('2FA sudah aktif', 400);
        }

        $secret = $this->google2fa->generateSecretKey();
        $user->update(['two_factor_secret' => $secret]);

        $qrCodeUrl = $this->google2fa->getQRCodeUrl(
            config('app.name'),
            $user->email,
            $secret
        );

        return $this->success([
            'secret'   => $secret,
            'qr_url'   => $qrCodeUrl,
        ], 'Scan QR code lalu konfirmasi dengan kode TOTP');
    }

    public function confirm(Request $request): JsonResponse
    {
        $request->validate(['code' => 'required|string|size:6']);

        $user = $request->user();

        if (!$user->two_factor_secret) {
            return $this->error('2FA belum diinisiasi, panggil enable dulu', 400);
        }

        $valid = $this->google2fa->verifyKey($user->two_factor_secret, $request->code);

        if (!$valid) {
            return $this->error('Kode TOTP tidak valid', 422);
        }

        $user->update(['two_factor_confirmed_at' => now()]);

        return $this->success(null, '2FA berhasil diaktifkan');
    }

    public function disable(Request $request): JsonResponse
    {
        $request->validate(['code' => 'required|string|size:6']);

        $user = $request->user();

        if (!$user->two_factor_confirmed_at) {
            return $this->error('2FA tidak aktif', 400);
        }

        $valid = $this->google2fa->verifyKey($user->two_factor_secret, $request->code);

        if (!$valid) {
            return $this->error('Kode TOTP tidak valid', 422);
        }

        $user->update([
            'two_factor_secret'       => null,
            'two_factor_confirmed_at' => null,
        ]);

        return $this->success(null, '2FA berhasil dinonaktifkan');
    }

    public function challenge(Request $request): JsonResponse
    {
        $request->validate([
            'temp_token' => 'required|string',
            'code'       => 'required|string|size:6',
        ]);

        // Temukan token sementara (ability: 2fa-challenge)
        $token = \Laravel\Sanctum\PersonalAccessToken::findToken($request->temp_token);

        if (!$token || !$token->can('2fa-challenge')) {
            return $this->error('Token tidak valid atau sudah expired', 401);
        }

        $user = $token->tokenable;

        $valid = $this->google2fa->verifyKey($user->two_factor_secret, $request->code);

        if (!$valid) {
            return $this->error('Kode TOTP tidak valid', 422);
        }

        // Hapus temp token, buat token permanen
        $token->delete();
        $authToken = $user->createToken('auth_token')->plainTextToken;

        return $this->success(['token' => $authToken], 'Login berhasil');
    }
}
