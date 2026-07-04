<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\StreamedResponse;

class ExportController extends Controller
{
    public function transactions(Request $request): StreamedResponse
    {
        $query = $request->user()->transactions()->with(['category', 'wallet'])
            ->orderBy('date');

        if ($request->filled('month')) {
            $query->whereMonth('date', $request->integer('month'));
        }
        if ($request->filled('year')) {
            $query->whereYear('date', $request->integer('year'));
        }

        $transactions = $query->get();
        $filename     = 'transactions-' . now()->format('Y-m-d') . '.csv';

        return response()->streamDownload(function () use ($transactions) {
            $handle = fopen('php://output', 'w');
            fputcsv($handle, ['date', 'title', 'category', 'wallet', 'type', 'amount', 'note']);

            foreach ($transactions as $tx) {
                fputcsv($handle, [
                    $tx->date?->format('Y-m-d'),
                    $tx->title,
                    $tx->category?->name,
                    $tx->wallet?->name,
                    $tx->type,
                    $tx->amount,
                    $tx->note,
                ]);
            }

            fclose($handle);
        }, $filename, ['Content-Type' => 'text/csv']);
    }
}
