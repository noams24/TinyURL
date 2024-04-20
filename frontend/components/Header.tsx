import { IoArrowRedoCircle } from "react-icons/io5";

const Header = () => {
  return (
    <nav className="sticky top-0 z-50 flex items-center justify-between py-5 px-10 max-h-24 shadow-md bg-white">
      <div className="flex items-center gap-4 font-semibold">
        <IoArrowRedoCircle className="text-green-500" size={34} />
        <p className=" text-lg font-bold">TinyURL Clone</p>
      </div>
    </nav>
  );
};

export default Header;