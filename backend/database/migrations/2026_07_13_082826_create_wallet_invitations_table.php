<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('wallet_invitations', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('wallet_id')->constrained()->cascadeOnDelete();
            $table->foreignId('inviter_id')->constrained('users')->cascadeOnDelete();
            $table->string('email');
            $table->string('token', 64)->unique();
            $table->enum('status', ['pending', 'accepted', 'declined'])->default('pending');
            $table->timestamp('expires_at');
            $table->timestamps();

            $table->index(['email', 'status']);
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('wallet_invitations');
    }
};
