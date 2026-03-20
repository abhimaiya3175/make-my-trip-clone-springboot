import React from "react";
import Head from "next/head";
import Footer from "@/components/Footer";
import FlightStatusTracker from "@/components/Flights/FlightStatusTracker";

const FlightStatusPage: React.FC = () => {
  return (
    <>
      <Head>
        <title>Flight Status | MakeMyTrip</title>
        <meta name="description" content="Track real-time flight status" />
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <main className="min-h-screen bg-gray-50 py-8">
        <div className="container mx-auto px-4">
          <h1 className="text-3xl font-bold text-center mb-8">Live Flight Status Tracker</h1>
          <FlightStatusTracker />
        </div>
      </main>

      <Footer />
    </>
  );
};

export default FlightStatusPage;
