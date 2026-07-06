<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\BudgetController;
use App\Http\Controllers\Api\CategoryController;
use App\Http\Controllers\Api\EmailVerificationController;
use App\Http\Controllers\Api\ExportController;
use App\Http\Controllers\Api\PasswordResetController;
use App\Http\Controllers\Api\SavingsGoalController;
use App\Http\Controllers\Api\StatisticsController;
use App\Http\Controllers\Api\TransactionController;
use App\Http\Controllers\Api\TwoFactorController;
use App\Http\Controllers\Api\WalletController;
use Illuminate\Support\Facades\Route;

Route::prefix('auth')->group(function () {
    Route::post('register', [AuthController::class, 'register'])->middleware('throttle:10,1');
    Route::post('login', [AuthController::class, 'login'])->middleware('throttle:5,1');

    // Password reset (public)
    Route::post('forgot-password', [PasswordResetController::class, 'forgotPassword'])->middleware('throttle:5,1');
    Route::post('reset-password', [PasswordResetController::class, 'resetPassword'])->middleware('throttle:5,1')->name('password.reset');

    // 2FA challenge — pakai temp_token, tidak butuh sanctum middleware penuh
    Route::post('two-factor-challenge', [TwoFactorController::class, 'challenge'])->middleware('throttle:10,1');

    Route::middleware('auth:sanctum')->group(function () {
        Route::post('logout', [AuthController::class, 'logout']);
        Route::get('me', [AuthController::class, 'me']);

        // Email verification
        Route::post('email/verification-notification', [EmailVerificationController::class, 'sendVerification'])->middleware('throttle:3,1');
        Route::get('email/verify/{id}/{hash}', [EmailVerificationController::class, 'verify'])->middleware('signed')->name('verification.verify');

        // 2FA management
        Route::post('two-factor-authentication', [TwoFactorController::class, 'enable']);
        Route::post('two-factor-authentication/confirm', [TwoFactorController::class, 'confirm']);
        Route::delete('two-factor-authentication', [TwoFactorController::class, 'disable']);
    });
});

Route::middleware('auth:sanctum')->group(function () {
    Route::apiResource('wallets', WalletController::class);
    Route::apiResource('categories', CategoryController::class);
    Route::apiResource('transactions', TransactionController::class);

    // Budget — upsert via POST, tidak ada PUT
    Route::get('budgets', [BudgetController::class, 'index']);
    Route::post('budgets', [BudgetController::class, 'store']);
    Route::delete('budgets/{budget}', [BudgetController::class, 'destroy']);

    // Savings goals
    Route::apiResource('savings', SavingsGoalController::class)->except(['show']);

    Route::prefix('statistics')->group(function () {
        Route::get('summary', [StatisticsController::class, 'summary']);
        Route::get('by-category', [StatisticsController::class, 'byCategory']);
        Route::get('by-wallet', [StatisticsController::class, 'byWallet']);
        Route::get('monthly', [StatisticsController::class, 'monthly']);
    });

    Route::get('export/transactions', [ExportController::class, 'transactions']);
});
