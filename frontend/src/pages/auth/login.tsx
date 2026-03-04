import React from "react";
import LoginForm from "@/components/auth/LoginForm";
import { useRouter } from "next/router";

export default function LoginPage() {
  const router = useRouter();

  const handleLoginSuccess = (user: any) => {
    router.push("/");
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full p-6 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold text-center mb-6">Login</h1>
        <LoginForm
          onSuccess={handleLoginSuccess}
          onSwitchToSignup={() => router.push("/auth/signup")}
        />
      </div>
    </div>
  );
}
