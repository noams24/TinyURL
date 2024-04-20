"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import axios from "axios";
import Link from "next/link";
import { useState } from "react";
import { FaCopy, FaDirections } from "react-icons/fa";
import { Drawer, Snackbar } from "@mui/material";
import Alert from "@mui/material/Alert";
import DrawerList from "@/components/DrawerList";

export default function Home() {
  const [tinyUrl, setTinyUrl] = useState(null);
  const [inputUrl, setInputUrl] = useState("");
  const [isCopied, setIsCopied] = useState(false);
  const [drawerData, setDrawerData] = useState(null);

  const [openDrawer, setOpenDrawer] = useState(false);

  const toggleDrawer = (newOpen: boolean) => () => {
    setOpenDrawer(newOpen);
  };

  const HandleInputChange = (event: any) => {
    setInputUrl(event.target.value);
  };

  const handleClick = async () => {
    const body = JSON.stringify({
      longUrl: inputUrl,
      userName: "noam",
    });

    try {
      const data: any = await axios({
        method: "post",
        url: "http://localhost:8080/tiny",
        data: body,
        headers: {
          "Content-Type": "application/json",
        },
      }).then((data) => {
        setTinyUrl(data.data);
      });
    } catch (e) {
      console.log(e);
    }

    getDrawerData();
  };

  const handleCopy = (keyUrl: String) => {
    //@ts-ignore
    navigator.clipboard.writeText(keyUrl).then(() => {
      setIsCopied(true);
      setTimeout(() => {
        setIsCopied(false);
      }, 2000);
    });
  };

  const handleClear = () => {
    setInputUrl("");
    setTinyUrl(null);
  };

  const getDrawerData = async () => {
    try {
      const data: any = await axios({
        method: "get",
        url: "http://localhost:8080/user/{name}?name=noam",
      }).then((data) => {
        setDrawerData(data.data.shorts);
      });
    } catch (e) {
      console.log(e);
    }
  };

  const handleMyURLS = () => {
    getDrawerData();
    setOpenDrawer(true);
  };

  return (
    <>
      <Drawer anchor={"right"} open={openDrawer} onClose={toggleDrawer(false)}>
        <DrawerList
          toggleDrawer={toggleDrawer}
          handleCopy={handleCopy}
          data={drawerData}
        />
      </Drawer>

      <Snackbar
        open={isCopied}
        autoHideDuration={6000}
        anchorOrigin={{
          vertical: "top",
          horizontal: "center",
        }}
      >
        <Alert severity="success" sx={{ width: "100%" }}>
          Link Copied to Clipboard
        </Alert>
      </Snackbar>
      <div className="flex justify-center items-center">
        <div className="min-w-64 border rounded-md border-black shadow-md">
          <div className="p-4 items-center">
            {tinyUrl === null ? (
              <>
                <p>Shorten a long URL</p>
                <Input
                  value={inputUrl}
                  onChange={HandleInputChange}
                  placeholder="Enter long link here"
                  className="mt-3"
                />
                <Button
                  onClick={handleClick}
                  className="mt-3 w-full"
                  type="submit"
                >
                  ShortenURL
                </Button>
              </>
            ) : (
              <>
                <p> Your Long URL:</p>
                <p className="p-2 mt-2 border border-slate-300 rounded-md text-green-600">
                  {inputUrl}
                </p>
                <p className="mt-6">Your short URL is:</p>
                <p className="p-2 mt-2 border border-slate-300 rounded-md text-green-600">
                  {tinyUrl}
                </p>
                <div className="flex justify-center gap-2 mt-2">
                  {/*@ts-ignore */}
                  <Link href={tinyUrl} target="_blank">
                    <Button className="bg-blue-500 hover:bg-slate-800">
                      <FaDirections />
                      Visit
                    </Button>
                  </Link>
                  <Button onClick={() => handleCopy(tinyUrl)}>
                    <FaCopy />
                    Copy
                  </Button>
                </div>
                <div className="flex justify-center mt-2 gap-3">
                  <Button
                    onClick={handleMyURLS}
                    className="h-12 bg-white text-green-800 border border-green-800"
                  >
                    My URLs
                  </Button>
                  <Button onClick={handleClear} className="h-12">
                    Shorten another
                  </Button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
