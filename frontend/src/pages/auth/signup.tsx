import React from "react";
import SignupForm from "@/components/auth/SignupForm";
import { useRouter } from "next/router";

export default function SignupPage() {
  const router = useRouter();

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full p-6 bg-white rounded-lg shadow-md">
        <h1 className="text-2xl font-bold text-center mb-6">Sign Up</h1>
        <SignupForm />
        <p className="text-center text-sm mt-4">
          Already have an account?{" "}
          <button onClick={() => router.push("/auth/login")} className="text-blue-500 underline">
            Login
          </button>
        </p>
      </div>
    </div>
  );
}
