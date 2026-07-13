<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('wallet_user', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('wallet_id')->constrained()->cascadeOnDelete();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->string('role')->default('member'); // owner tersimpan di wallets.user_id; pivot hanya untuk member
            $table->timestamps();

            $table->unique(['wallet_id', 'user_id']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('wallet_user');
    }
};
