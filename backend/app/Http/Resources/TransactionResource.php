<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class TransactionResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id'          => $this->id,
            'user_id'     => $this->user_id,
            'category_id' => $this->category_id,
            'wallet_id'   => $this->wallet_id,
            'title'       => $this->title,
            'amount'      => $this->amount,
            'type'        => $this->type,
            'date'        => $this->date?->format('Y-m-d'),
            'note'        => $this->note,
            'category'    => new CategoryResource($this->whenLoaded('category')),
            'wallet'      => new WalletResource($this->whenLoaded('wallet')),
        ];
    }
}
