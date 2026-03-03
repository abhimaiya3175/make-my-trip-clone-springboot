import "@/styles/globals.css";
import type { AppProps } from "next/app";
import Head from "next/head";
import store, { setUser } from "@/store";
import { Provider } from "react-redux";
import Navbar from "@/components/Navbar";

import { useEffect } from "react";
import Footer from "@/components/Fotter";

const Myapp = ({ Component, pageProps }: AppProps) => {
  useEffect(() => {
    const storeduser = localStorage.getItem("user");
    if (storeduser) {
      store.dispatch(setUser(JSON.parse(storeduser)));
    }
  }, []);
  return (
    <div className="min-h-screen ">
      <Navbar />
      <Component {...pageProps} />
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
