<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CategoryController;
use App\Http\Controllers\Api\ExportController;
use App\Http\Controllers\Api\StatisticsController;
use App\Http\Controllers\Api\TransactionController;
use App\Http\Controllers\Api\WalletController;
use Illuminate\Support\Facades\Route;

Route::prefix('auth')->group(function () {
    Route::post('register', [AuthController::class, 'register']);
    Route::post('login', [AuthController::class, 'login']);
    Route::middleware('auth:sanctum')->group(function () {
        Route::post('logout', [AuthController::class, 'logout']);
        Route::get('me', [AuthController::class, 'me']);
    });
});

Route::middleware('auth:sanctum')->group(function () {
    Route::apiResource('wallets', WalletController::class);
    Route::apiResource('categories', CategoryController::class);
    Route::apiResource('transactions', TransactionController::class);

    Route::prefix('statistics')->group(function () {
        Route::get('summary', [StatisticsController::class, 'summary']);
        Route::get('by-category', [StatisticsController::class, 'byCategory']);
        Route::get('by-wallet', [StatisticsController::class, 'byWallet']);
        Route::get('monthly', [StatisticsController::class, 'monthly']);
    });

    Route::get('export/transactions', [ExportController::class, 'transactions']);
});
