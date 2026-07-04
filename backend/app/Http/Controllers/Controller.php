<?php

namespace App\Http\Controllers;

abstract class Controller
{
    protected function success(mixed $data, string $message = 'OK', int $status = 200): \Illuminate\Http\JsonResponse
    {
        return response()->json(['data' => $data, 'message' => $message, 'status' => true], $status);
    }

    protected function error(string $message, int $status = 400): \Illuminate\Http\JsonResponse
    {
        return response()->json(['data' => null, 'message' => $message, 'status' => false], $status);
    }
}
