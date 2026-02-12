<?php

namespace App\Controllers;

use App\Models\User;

class UserController extends BaseController {
    public function index($id) {
        $user = User::find($id);
        if (!$user) {
            return $this->error("User not found", 404);
        }
        return view('user.profile', ['user' => $user]);
    }

    private function validate(array $data) : bool {
        return isset($data['email']) && filter_var($data['email'], FILTER_VALIDATE_EMAIL);
    }
}

$controller = new UserController();
$controller->index(1);
?>