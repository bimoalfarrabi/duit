<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Wallet extends Model
{
    use HasFactory;

    protected $fillable = ['user_id', 'name', 'type', 'color', 'icon', 'balance'];

    protected $casts = [
        'balance' => 'decimal:2',
    ];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function transactions(): HasMany
    {
        return $this->hasMany(Transaction::class);
    }

    /** Member yang di-share (owner tidak masuk pivot, dilacak via user_id). */
    public function members(): BelongsToMany
    {
        return $this->belongsToMany(User::class, 'wallet_user')
            ->withPivot('role')
            ->withTimestamps();
    }

    public function invitations(): HasMany
    {
        return $this->hasMany(WalletInvitation::class);
    }

    public function isOwnedBy(User $user): bool
    {
        return $this->user_id === $user->id;
    }

    /** Owner atau member yang sudah accept. */
    public function isAccessibleBy(User $user): bool
    {
        return $this->isOwnedBy($user)
            || $this->members()->whereKey($user->id)->exists();
    }
}
