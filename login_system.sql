-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 18, 2026 at 04:32 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `login_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `activity_logs`
--

CREATE TABLE `activity_logs` (
  `id` int(11) NOT NULL,
  `username` varchar(100) DEFAULT NULL,
  `action_done` varchar(255) DEFAULT NULL,
  `date_created` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `activity_logs`
--

INSERT INTO `activity_logs` (`id`, `username`, `action_done`, `date_created`) VALUES
(1, 'DanPogi', 'Placed order: ORD-20260518-001', '2026-05-18 01:33:38');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `contact` varchar(50) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`id`, `name`, `contact`, `address`) VALUES
(2, 'Dan', '0929361816', 'Antipolo City');

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `id` int(11) NOT NULL,
  `order_code` varchar(50) DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  `order_date` datetime DEFAULT NULL,
  `customer_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`id`, `order_code`, `total_amount`, `order_date`, `customer_id`) VALUES
(1, 'TEST-001', 1000, '2026-04-25 21:00:00', NULL),
(2, 'ORD-20260425-002', 25000, '2026-04-25 21:06:38', NULL),
(3, 'ORD-20260427-001', 25000, '2026-04-27 13:43:17', NULL),
(4, 'ORD-20260427-002', 25000, '2026-04-27 14:35:14', NULL),
(5, 'ORD-20260427-003', 15000, '2026-04-27 15:06:53', NULL),
(6, 'ORD-20260503-001', 7000, '2026-05-03 15:01:21', NULL),
(7, 'ORD-20260504-001', 11500, '2026-05-04 09:43:39', NULL),
(10, 'ORD-20260504-002', 250, '2026-05-04 11:18:55', NULL),
(11, 'ORD-20260504-003', 70000, '2026-05-04 11:20:51', NULL),
(12, 'ORD-20260504-004', 140000, '2026-05-04 11:22:35', NULL),
(13, 'ORD-20260518-001', 149000, '2026-05-18 09:33:38', 2);

-- --------------------------------------------------------

--
-- Table structure for table `order_details`
--

CREATE TABLE `order_details` (
  `id` int(11) NOT NULL,
  `order_id` int(11) DEFAULT NULL,
  `product` varchar(100) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `subtotal` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_details`
--

INSERT INTO `order_details` (`id`, `order_id`, `product`, `price`, `quantity`, `subtotal`) VALUES
(1, 2, 'Excavator (Rent) ', 5000, 5, 25000),
(2, 3, 'Excavator (Rent) ', 5000, 5, 25000),
(3, 4, 'Excavator (Rent)', 5000, 5, 25000),
(4, 5, 'Excavator (Rent)', 5000, 3, 15000),
(5, 6, 'Excavator (Rent)', 7000, 1, 7000),
(6, 7, 'Excavator (Rent)', 7000, 1, 7000),
(7, 7, 'Coco lumber', 300, 15, 4500),
(8, 10, 'Cement (Sale)', 250, 1, 250),
(9, 11, 'Excavator (Rent)', 7000, 10, 70000),
(10, 12, 'Excavator (Rent)', 7000, 20, 140000),
(11, 13, 'Excavator (Rent)', 6000, 5, 30000),
(12, 13, 'Bulldozer (Rent)', 4600, 5, 23000),
(13, 13, 'Sand (Sale)', 1200, 25, 30000),
(14, 13, 'Metals (Sale)', 3000, 15, 45000),
(15, 13, 'Good lumber', 700, 30, 21000);

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `id` int(11) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `stock` int(11) DEFAULT 0,
  `barcode` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`id`, `name`, `price`, `stock`, `barcode`) VALUES
(1, 'Excavator (Rent)', 6000, 15, NULL),
(2, 'Bulldozer (Rent)', 4600, 10, NULL),
(3, 'Dump Truck (Rent)', 4000, 15, NULL),
(4, 'Wheel Loader (Rent)', 4200, 15, NULL),
(5, 'Crusher (Rent)', 6000, 15, NULL),
(6, 'Sand (Sale)', 1200, 25, NULL),
(7, 'Gravel (Sale)', 1500, 50, NULL),
(8, 'Cement (Sale)', 250, 99, NULL),
(9, 'Hollow Blocks (Sale)', 10, 1000, NULL),
(10, 'Metals (Sale)', 3000, 5, NULL),
(17, 'Coco lumber', 300, 20, NULL),
(18, 'Good lumber', 700, 30, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `mother_name` varchar(100) NOT NULL,
  `favorite_color` varchar(100) NOT NULL,
  `birthday` varchar(50) NOT NULL,
  `role` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `mother_name`, `favorite_color`, `birthday`, `role`) VALUES
(3, 'DanPogi', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Awie', 'Black', '2006-07-29', 'admin'),
(4, 'admin123', 'bfec58f1887ebeb3392a7d0f21c928d76ecc8a5680a57fb61ad894f4aa152a73', 'aura', 'nigga', '2006-07-30', 'customer'),
(6, 'D@n@_adm!n321!', '556bd3203dfa24595efa44b56ccc07e80b53226d5434aa8bf921e78d699eb9af', 'aaura', 'bblack', '2006-07-29', 'admin'),
(7, 'admin321_@Dan', 'b5f04fc1502092b0957d3e758f07a1b07c64688bd06ce4dc2b133a7f3c0e3010', 'aawie', 'nnigga', '2006-07-30', 'customer');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_customer` (`customer_id`);

--
-- Indexes for table `order_details`
--
ALTER TABLE `order_details`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activity_logs`
--
ALTER TABLE `activity_logs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `order_details`
--
ALTER TABLE `order_details`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
