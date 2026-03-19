import React from "react";
import SignupDialog from "./SignupDialog";
import {
  LogOut,
  Plane,
  User,
  FileX2,
  Home,
  Building2,
  ClipboardList,
  Armchair,
  BadgeIndianRupee,
  MessageSquareText,
} from "lucide-react";
import { useDispatch, useSelector } from "react-redux";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "./ui/button";
import { Avatar, AvatarFallback } from "./ui/avatar";
import { clearUser } from "@/store";
import { useRouter } from "next/router";
import Link from "next/link";

const Navbar = () => {
  const dispatch = useDispatch();
  const user = useSelector((state: any) => state.user.user);
  const router = useRouter();
  const logout = () => {
    dispatch(clearUser());
    router.push("/");
  };

  const navLinks = [
    { href: "/", label: "Home", icon: <Home className="w-4 h-4" /> },
    { href: "/flights", label: "Flights", icon: <Plane className="w-4 h-4" /> },
    { href: "/hotels", label: "Hotels", icon: <Building2 className="w-4 h-4" /> },
  ];

  const authNavLinks = [
    { href: "/bookings", label: "Bookings", icon: <ClipboardList className="w-4 h-4" /> },
    { href: "/seat-room", label: "Seat & Room", icon: <Armchair className="w-4 h-4" /> },
    { href: "/pricing", label: "Pricing", icon: <BadgeIndianRupee className="w-4 h-4" /> },
    { href: "/flight-status", label: "Flight Status", icon: <Plane className="w-4 h-4" /> },
    { href: "/recommendations/suggestions", label: "Recommendations", icon: <Building2 className="w-4 h-4" /> },
    { href: "/reviews/review", label: "Reviews", icon: <MessageSquareText className="w-4 h-4" /> },
    { href: "/cancellations", label: "Cancellations", icon: <FileX2 className="w-4 h-4" /> },
    { href: "/profile", label: "Profile", icon: <User className="w-4 h-4" /> },
  ];

  return (
    <header className="bg-white shadow-sm backdrop-blur-md py-3 sticky top-0 z-50">
      <div className="container mx-auto px-4 flex items-center justify-between">
        {/* Logo */}
        <Link href="/" className="flex items-center space-x-2 cursor-pointer">
          <Plane className="w-8 h-8 text-red-500" />
          <span className="text-2xl font-bold text-black">MakeMyTour</span>
        </Link>

        {/* Center Navigation Links */}
        <nav className="hidden md:flex items-center space-x-1">
          {navLinks.map((link) => (
            <Link
              key={link.label}
              href={link.href}
              className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${router.pathname === link.href
                  ? "text-red-600 bg-red-50"
                  : "text-gray-600 hover:text-red-600 hover:bg-gray-50"
                }`}
            >
              {link.icon}
              {link.label}
            </Link>
          ))}
          {user &&
            authNavLinks.map((link) => (
              <Link
                key={link.label}
                href={link.href}
                className={`flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${router.pathname === link.href
                    ? "text-red-600 bg-red-50"
                    : "text-gray-600 hover:text-red-600 hover:bg-gray-50"
                  }`}
              >
                {link.icon}
                {link.label}
              </Link>
            ))}
        </nav>

        {/* Right section */}
        <div className="flex items-center space-x-3">
          {user ? (
            <>
              {user.role === "ADMIN" && (
                <Button variant="default" onClick={() => router.push("/admin")}>
                  ADMIN
                </Button>
              )}

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant="ghost"
                    className="relative h-8 w-8 rounded-full"
                  >
                    <Avatar className="h-8 w-8">
                      <AvatarFallback>
                        {user?.firstName?.charAt(0)}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm font-medium leading-none">
                        {user?.firstName}
                      </p>
                      <p className="text-xs leading-none text-muted-foreground">
                        {user?.email}
                      </p>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => router.push("/profile")}>
                    <User className="mr-2 h-4 w-4" />
                    <span>Profile</span>
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => router.push("/cancellations")}>
                    <FileX2 className="mr-2 h-4 w-4" />
                    <span>My Cancellations</span>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => logout()}>
                    <LogOut className="mr-2 h-4 w-4" />
                    <span>Log out</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <SignupDialog
              trigger={
                <Button
                  variant="outline"
                  className="bg-blue-600 text-white hover:bg-blue-700"
                >
                  Login
                </Button>
              }
            />
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
