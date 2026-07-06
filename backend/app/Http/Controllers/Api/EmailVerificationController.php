<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Auth\Events\Verified;
use Illuminate\Foundation\Auth\EmailVerificationRequest;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class EmailVerificationController extends Controller
{
    public function sendVerification(Request $request): JsonResponse
    {
        if ($request->user()->hasVerifiedEmail()) {
            return $this->error('Email sudah terverifikasi', 400);
        }

        $request->user()->sendEmailVerificationNotification();

        return $this->success(null, 'Link verifikasi telah dikirim');
    }

    public function verify(EmailVerificationRequest $request): JsonResponse
    {
        if ($request->user()->hasVerifiedEmail()) {
            return $this->success(null, 'Email sudah terverifikasi');
        }

        $request->fulfill();

        return $this->success(null, 'Email berhasil diverifikasi');
    }
}
