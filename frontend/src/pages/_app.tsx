import "@/styles/globals.css";
import type { AppProps } from "next/app";
import Head from "next/head";
import store, { setUser } from "@/store";
import { Provider } from "react-redux";
import Navbar from "@/components/Navbar";
import ErrorBoundary from "@/components/ErrorBoundary";

import { useEffect } from "react";
import Footer from "@/components/Footer";
import { useRouter } from "next/router";

const Myapp = ({ Component, pageProps }: AppProps) => {
  const router = useRouter();
  const isHomePage = router.pathname === "/";

  useEffect(() => {
    const storeduser = localStorage.getItem("user");
    if (storeduser) {
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
    </Provider>
  );
}
