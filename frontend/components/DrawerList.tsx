import { Button } from "./ui/button";
import Link from "next/link";
import { FaCopy, FaDirections } from "react-icons/fa";
import { AiOutlineClose } from "react-icons/ai";
//

interface TinyUrl {
  originalUrl: string;
  shortUrl: string;
  clicks: number;
}

const DrawerList = ({ toggleDrawer, handleCopy, data }: any) => {

  return (
    <div className="mt-5">
      <div className="flex justify-between">
        <h1 className=" text-2xl font-bold">Your recent TinyURLs</h1>
        <button
          className="mr-4 border h-8 w-8 flex justify-center items-center"
          onClick={toggleDrawer(false)}
        >
          <AiOutlineClose />
        </button>
      </div>
      <div className="flex flex-col space-y-2 mt-10">
        {data && Object.keys(data).map(key => {
        const { longUrl, totalClicks, clicks } = data[key];
        const keyUrl = `http://localhost:8080/${key}/`
        return (
          <div key={key} className="border border-zinc-400 rounded-sm mx-3 p-3">
            <p className="font-bold text-lg">{keyUrl}</p>
            <p className="text-green-700">Long URL: {longUrl}</p>
            <p className=" text-sm text-blue-700">Total Clicks: {totalClicks !== null ? totalClicks : 0}</p>
            <div className="flex  gap-2 mt-2">
              <Link href={keyUrl} target="_blank">
                <Button className="bg-blue-500 hover:bg-slate-800">
                  <FaDirections />
                  Visit
                </Button>
              </Link>
              <Button onClick={() => handleCopy(keyUrl)}>
                <FaCopy />
                Copy
              </Button>
            </div>
          </div>
        );
      })}
      </div>
    </div>
  );
};

export default DrawerList;