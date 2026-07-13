<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class WalletInvitation extends Model
{
    use HasFactory;

    protected $fillable = ['wallet_id', 'inviter_id', 'email', 'token', 'status', 'expires_at'];

    protected $casts = [
        'expires_at' => 'datetime',
    ];

    public function wallet(): BelongsTo
    {
        return $this->belongsTo(Wallet::class);
    }

    public function inviter(): BelongsTo
    {
        return $this->belongsTo(User::class, 'inviter_id');
    }

    public function isExpired(): bool
    {
        return $this->expires_at->isPast();
    }
}
