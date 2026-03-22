import "@/styles/globals.css";
import type { AppProps } from "next/app";
import Head from "next/head";
import store, { clearUser, setUser } from "@/store";
import { Provider } from "react-redux";
import Navbar from "@/components/Navbar";
import ErrorBoundary from "@/components/ErrorBoundary";
import { Toaster } from "@/components/ui/sonner";

import { useEffect } from "react";
import Footer from "@/components/Footer";
import { useRouter } from "next/router";

const Myapp = ({ Component, pageProps }: AppProps) => {
  const router = useRouter();
  const isHomePage = router.pathname === "/";

  useEffect(() => {
    const authToken = localStorage.getItem("authToken");
    const storeduser = localStorage.getItem("user");

    // Session migration guard: legacy sessions may contain user but no JWT token.
    if (storeduser && !authToken) {
      store.dispatch(clearUser());
      return;
    }

    if (storeduser && authToken) {
      store.dispatch(setUser(JSON.parse(storeduser)));
    }
  }, []);
  return (
    <div className="min-h-screen ">
      {!isHomePage && <Navbar />}
      <ErrorBoundary>
        <Component {...pageProps} />
      </ErrorBoundary>
      <Footer/>
    </div>
  );
};
export default function App(props: AppProps) {
  return (
    <Provider store={store}>
      <Head>
        <title>MakeMyTour</title>
      </Head>
      <Myapp {...props} />
      <Toaster />
    </Provider>
  );
}
