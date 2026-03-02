package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zeus.ims.entity.*;
import org.zeus.ims.repository.*;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class SampleDataService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerPersonnelRepository customerPersonnelRepository;
    private final VendorRepository vendorRepository;
    private final VendorPersonnelRepository vendorPersonnelRepository;
    private final ProductRepository productRepository;
    private final ProductPartRepository productPartRepository;
    private final EnquiryRepository enquiryRepository;
    private final EnquiryItemRepository enquiryItemRepository;
    private final EnquiryConversationRepository enquiryConversationRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    // Helper class for structured address data
    private static class AddressData {
        private final String addressLine1;
        private final String addressLine2;
        private final String city;
        private final String state;
        private final String postalCode;
        private final String country;

        public AddressData(String addressLine1, String addressLine2, String city, String state, String postalCode, String country) {
            this.addressLine1 = addressLine1;
            this.addressLine2 = addressLine2;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }

        public String getAddressLine1() {
            return addressLine1;
        }

        public String getAddressLine2() {
            return addressLine2;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getCountry() {
            return country;
        }
    }

    @Autowired
    public SampleDataService(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           CustomerPersonnelRepository customerPersonnelRepository,
                           VendorRepository vendorRepository,
                           VendorPersonnelRepository vendorPersonnelRepository,
                           ProductRepository productRepository,
                           ProductPartRepository productPartRepository,
                           EnquiryRepository enquiryRepository,
                           EnquiryItemRepository enquiryItemRepository,
                           EnquiryConversationRepository enquiryConversationRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.customerPersonnelRepository = customerPersonnelRepository;
        this.vendorRepository = vendorRepository;
        this.vendorPersonnelRepository = vendorPersonnelRepository;
        this.productRepository = productRepository;
        this.productPartRepository = productPartRepository;
        this.enquiryRepository = enquiryRepository;
        this.enquiryItemRepository = enquiryItemRepository;
        this.enquiryConversationRepository = enquiryConversationRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initializeSampleData() {
        if (userRepository.count() == 0) {
            createSampleUsers();
        }
//        createSampleCustomers();
//        createSampleVendors();
//        createSampleProducts();
//        createSampleEnquiries();
//        createSampleOrders();

    }

    private void createSampleUsers() {
        List<User> users = Arrays.asList(
            createUser("admin", "admin@zeus.com", "Zeus Admin", UserRole.OWNER),
            createUser("sales_manager", "sales.manager@zeus.com", "Sarah Johnson", UserRole.SALES),
            createUser("sales_rep1", "sales.rep1@zeus.com", "Mike Chen", UserRole.SALES),
            createUser("sales_rep2", "sales.rep2@zeus.com", "Lisa Rodriguez", UserRole.SALES),
            createUser("production_mgr", "production.mgr@zeus.com", "David Kumar", UserRole.PRODUCTION_MANAGER),
            createUser("workshop_lead", "workshop.lead@zeus.com", "Robert Smith", UserRole.WORKSHOP_PERSONNEL),
            createUser("workshop_tech1", "workshop.tech1@zeus.com", "James Wilson", UserRole.WORKSHOP_PERSONNEL),
            createUser("workshop_tech2", "workshop.tech2@zeus.com", "Maria Garcia", UserRole.WORKSHOP_PERSONNEL),
            createUser("accountant", "accountant@zeus.com", "Jennifer Lee", UserRole.ACCOUNTANT)
        );
        userRepository.saveAll(users);
    }

    private User createUser(String username, String email, String fullName, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setActive(true);
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        return user;
    }

    private void createSampleCustomers() {
        List<String> companyNames = Arrays.asList(
            "TechCorp Industries", "Global Manufacturing Ltd", "Precision Engineering Co",
            "Advanced Robotics Inc", "Industrial Solutions Pvt Ltd", "MegaTech Corporation",
            "Future Systems Ltd", "Innovative Designs Inc", "Smart Manufacturing Co",
            "Elite Engineering Works", "Modern Industries Ltd", "Progressive Tech Solutions",
            "Dynamic Systems Corp", "NextGen Manufacturing", "Premier Industrial Group",
            "Apex Engineering Ltd", "Superior Tech Industries", "Excellence Manufacturing",
            "Prime Solutions Inc", "Ultimate Engineering Co"
        );

        List<AddressData> addresses = Arrays.asList(
            new AddressData("123 Industrial Park", "Sector 15", "Gurgaon", "Haryana", "122001", "India"),
            new AddressData("456 Tech Valley", "Whitefield", "Bangalore", "Karnataka", "560066", "India"),
            new AddressData("789 Manufacturing Hub", "Hinjewadi", "Pune", "Maharashtra", "411057", "India"),
            new AddressData("321 Business District", "Andheri East", "Mumbai", "Maharashtra", "400069", "India"),
            new AddressData("654 IT Corridor", "Thoraipakkam", "Chennai", "Tamil Nadu", "600097", "India"),
            new AddressData("987 Industrial Area", "Udyog Vihar", "Gurgaon", "Haryana", "122016", "India")
        );

        for (int i = 0; i < 20; i++) {
            Customer customer = new Customer();
            customer.setCompanyName(companyNames.get(i));
            customer.setContactNumber(generatePhoneNumber());
            customer.setEmail(generateCompanyEmail(companyNames.get(i)));

            AddressData addressData = addresses.get(i % addresses.size());
            customer.setAddressLine1(addressData.getAddressLine1());
            customer.setAddressLine2(addressData.getAddressLine2());
            customer.setCity(addressData.getCity());
            customer.setState(addressData.getState());
            customer.setPostalCode(addressData.getPostalCode());
            customer.setCountry(addressData.getCountry());

            customer.setDescription("Leading " + getRandomIndustry() + " company specializing in high-quality manufacturing solutions.");
            customer.setActive(true);
            customer.setCreatedBy("admin");
            customer.setUpdatedBy("admin");

            Customer savedCustomer = customerRepository.save(customer);
            createCustomerPersonnel(savedCustomer);
        }
    }

    private void createCustomerPersonnel(Customer customer) {
        List<String> firstNames = Arrays.asList("John", "Sarah", "Michael", "Jennifer", "David", "Lisa", "Robert", "Maria", "James", "Emily");
        List<String> lastNames = Arrays.asList("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez");
        List<String> departments = Arrays.asList("Engineering", "Procurement", "Production", "Quality", "Sales", "Operations");
        List<String> designations = Arrays.asList("Manager", "Senior Engineer", "Lead", "Executive", "Specialist", "Coordinator");

        for (int i = 0; i < random.nextInt(3) + 2; i++) { // 2-4 personnel per customer
            CustomerPersonnel personnel = new CustomerPersonnel();
            String firstName = firstNames.get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));

            personnel.setFullName(firstName + " " + lastName);
            personnel.setContactNumber(generatePhoneNumber());
            personnel.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" +
                             customer.getCompanyName().toLowerCase().replaceAll("[^a-z0-9]", "") + ".com");
            personnel.setDepartment(departments.get(random.nextInt(departments.size())));
            personnel.setDesignation(designations.get(random.nextInt(designations.size())));
            personnel.setCustomer(customer);
            personnel.setActive(true);
            personnel.setCreatedBy("admin");
            personnel.setUpdatedBy("admin");

            customerPersonnelRepository.save(personnel);
        }
    }

    private void createSampleVendors() {
        List<String> vendorNames = Arrays.asList(
            "Steel Suppliers India Ltd", "Precision Parts Co", "Electronic Components Inc",
            "Hardware Solutions Pvt Ltd", "Metal Works Corporation", "Component Manufacturers Ltd",
            "Industrial Supplies Co", "Raw Materials Inc", "Fabrication Partners Ltd",
            "Quality Components Corp", "Reliable Suppliers Inc", "Premier Parts Ltd",
            "Advanced Materials Co", "Specialty Components Inc", "Industrial Resources Ltd",
            "Manufacturing Supplies Corp", "Component Specialists Ltd", "Material Solutions Inc",
            "Parts Manufacturers Co", "Supply Chain Partners Ltd"
        );

        List<AddressData> addresses = Arrays.asList(
            new AddressData("Plot 45, Industrial Estate", "", "Faridabad", "Haryana", "121003", "India"),
            new AddressData("Unit 12, Manufacturing Zone", "", "Hosur", "Tamil Nadu", "635109", "India"),
            new AddressData("Building 7, Industrial Park", "", "Aurangabad", "Maharashtra", "431001", "India"),
            new AddressData("Sector 8, Industrial Area", "", "Rajkot", "Gujarat", "360003", "India"),
            new AddressData("Plot 23, Export Promotion Zone", "", "Coimbatore", "Tamil Nadu", "641014", "India")
        );

        for (int i = 0; i < 20; i++) {
            Vendor vendor = new Vendor();
            vendor.setCompanyName(vendorNames.get(i));
            vendor.setContactNumber(generatePhoneNumber());
            vendor.setEmail(generateCompanyEmail(vendorNames.get(i)));

            AddressData addressData = addresses.get(i % addresses.size());
            vendor.setAddressLine1(addressData.getAddressLine1());
            vendor.setAddressLine2(addressData.getAddressLine2());
            vendor.setCity(addressData.getCity());
            vendor.setState(addressData.getState());
            vendor.setPostalCode(addressData.getPostalCode());
            vendor.setCountry(addressData.getCountry());

            vendor.setDescription("Trusted supplier of " + getRandomMaterial() + " with over " +
                                 (random.nextInt(20) + 5) + " years of experience.");
            vendor.setActive(true);
            vendor.setCreatedBy("admin");
            vendor.setUpdatedBy("admin");

            Vendor savedVendor = vendorRepository.save(vendor);
            createVendorPersonnel(savedVendor);
        }
    }

    private void createVendorPersonnel(Vendor vendor) {
        List<String> firstNames = Arrays.asList("Raj", "Priya", "Amit", "Kavya", "Suresh", "Meera", "Vikram", "Anita", "Ravi", "Pooja");
        List<String> lastNames = Arrays.asList("Sharma", "Patel", "Gupta", "Singh", "Kumar", "Reddy", "Agarwal", "Jain", "Shah", "Verma");
        List<String> departments = Arrays.asList("Sales", "Supply Chain", "Quality Control", "Business Development", "Operations");
        List<String> designations = Arrays.asList("Sales Manager", "Account Executive", "Supply Chain Lead", "Quality Manager", "Business Head");

        for (int i = 0; i < random.nextInt(2) + 2; i++) { // 2-3 personnel per vendor
            VendorPersonnel personnel = new VendorPersonnel();
            String firstName = firstNames.get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));

            personnel.setFullName(firstName + " " + lastName);
            personnel.setContactNumber(generatePhoneNumber());
            personnel.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" +
                             vendor.getCompanyName().toLowerCase().replaceAll("[^a-z0-9]", "") + ".com");
            personnel.setDepartment(departments.get(random.nextInt(departments.size())));
            personnel.setDesignation(designations.get(random.nextInt(designations.size())));
            personnel.setVendor(vendor);
            personnel.setActive(true);
            personnel.setCreatedBy("admin");
            personnel.setUpdatedBy("admin");

            vendorPersonnelRepository.save(personnel);
        }
    }

    private void createSampleProducts() {
        List<String> productNames = Arrays.asList(
            "Industrial Automation Controller", "Precision CNC Machining Center", "Robotic Assembly Unit",
            "Quality Control System", "Material Handling Equipment", "Testing & Measurement Device",
            "Manufacturing Execution System", "Process Control Unit", "Safety Monitoring System",
            "Data Acquisition Module", "Vision Inspection System", "Conveyor Control Panel",
            "Environmental Monitoring Unit", "Power Distribution System", "Motor Control Center",
            "SCADA Integration Module", "Temperature Control System", "Pressure Monitoring Device",
            "Flow Measurement System", "Vibration Analysis Equipment"
        );

        List<String> descriptions = Arrays.asList(
            "High-performance automation solution for industrial applications",
            "Precision engineering equipment for manufacturing excellence",
            "Advanced robotic system for automated production lines",
            "Comprehensive quality assurance and control system",
            "Efficient material transport and handling solution"
        );

        List<Vendor> vendors = vendorRepository.findAll();

        for (int i = 0; i < 20; i++) {
            Product product = new Product();
            product.setName(productNames.get(i));
            product.setDescription(descriptions.get(i % descriptions.size()) +
                                 ". Designed for high reliability and performance in demanding industrial environments.");
            product.setModelNumber("ZMS-" + String.format("%04d", i + 1));
            product.setCategory(getRandomProductCategory());
            product.setActive(true);
            product.setCreatedBy("admin");
            product.setUpdatedBy("admin");

            Product savedProduct = productRepository.save(product);
            createProductParts(savedProduct, vendors);
        }
    }

    private void createProductParts(Product product, List<Vendor> vendors) {
        List<String> partNames = Arrays.asList(
            "Steel Housing", "Control Circuit Board", "Power Supply Unit", "Cooling Fan Assembly",
            "Display Panel", "Sensor Module", "Actuator Unit", "Connector Assembly",
            "Filter Component", "Motor Assembly", "Gear Box", "Bearing Set",
            "Cable Harness", "Switch Assembly", "Valve Component", "Pump Unit"
        );

        List<String> materials = Arrays.asList("Steel", "Aluminum", "Plastic", "Copper", "Brass");
        List<String> units = Arrays.asList("mm", "cm", "inch");

        for (int i = 0; i < random.nextInt(8) + 5; // 5-12 parts per product
             i++) {
            ProductPart part = new ProductPart();
            part.setPartName(partNames.get(random.nextInt(partNames.size())));
            part.setPartNumber("PRT-" + String.format("%06d", random.nextInt(999999) + 1));
            part.setMaterial(materials.get(random.nextInt(materials.size())));

            String unit = units.get(random.nextInt(units.size()));
            part.setDimensions(String.format("%.1f x %.1f x %.1f %s",
                             random.nextDouble() * 100 + 10,
                             random.nextDouble() * 100 + 10,
                             random.nextDouble() * 50 + 5, unit));

            part.setVendor(vendors.get(random.nextInt(vendors.size())));
            part.setProduct(product);
            part.setActive(true);
            part.setCreatedBy("admin");
            part.setUpdatedBy("admin");

            productPartRepository.save(part);
        }
    }

    private void createSampleEnquiries() {
        List<Customer> customers = customerRepository.findAll();
        List<Product> products = productRepository.findAll();

        for (int i = 0; i < 25; i++) {
            Enquiry enquiry = new Enquiry();
            enquiry.setEnquiryNumber("ENQ-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")) +
                                   "-" + String.format("%04d", i + 1));
            enquiry.setCustomer(customers.get(random.nextInt(customers.size())));
            enquiry.setDescription("Enquiry for " + getRandomProjectType() + " implementation");
            enquiry.setRequirements("Customer requires " + getRandomRequirement() + " with delivery in " +
                                  (random.nextInt(12) + 4) + " weeks");
            enquiry.setStatus(getRandomEnquiryStatus());
            enquiry.setPriority(getRandomEnquiryPriority());
            enquiry.setTags(getRandomTags());

            LocalDateTime createdDate = LocalDateTime.now().minusDays(random.nextInt(90));
            enquiry.setDeliveryExpectedDate(createdDate.plusDays(random.nextInt(120) + 30));
            enquiry.setQuoteValidUntil(createdDate.plusDays(random.nextInt(30) + 15));
            enquiry.setActive(true);
            enquiry.setCreatedBy("sales_manager");
            enquiry.setUpdatedBy("sales_manager");

            Enquiry savedEnquiry = enquiryRepository.save(enquiry);
            createEnquiryItems(savedEnquiry, products);
            createEnquiryConversations(savedEnquiry);
        }
    }

    private void createEnquiryItems(Enquiry enquiry, List<Product> products) {
        System.out.println("creating enquiry items for enquiry: " + enquiry.getEnquiryNumber());
        int itemCount = random.nextInt(3) + 1; // 1-3 items per enquiry

        for (int i = 0; i < itemCount; i++) {
            EnquiryItem item = new EnquiryItem();
            item.setEnquiry(enquiry);
            item.setProduct(products.get(random.nextInt(products.size())));
            item.setQuantity(random.nextInt(10) + 1);

            BigDecimal unitPrice = BigDecimal.valueOf(random.nextDouble() * 50000 + 10000)
                                           .setScale(2, BigDecimal.ROUND_HALF_UP);
            item.setUnitPrice(unitPrice);
            item.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setSpecifications("Custom specifications for " + item.getProduct().getName());
            item.setRemarks("Special requirements noted during initial discussion");
            item.setCreatedBy("sales_manager");
            item.setUpdatedBy("sales_manager");

            enquiryItemRepository.save(item);
        }
    }

    private void createEnquiryConversations(Enquiry enquiry) {
        List<String> messages = Arrays.asList(
            "Initial enquiry received and logged in system",
            "Customer requirements reviewed and technical feasibility assessed",
            "Preliminary quote prepared and sent for review",
            "Customer feedback received on technical specifications",
            "Revised proposal prepared based on customer inputs",
            "Commercial terms discussed and negotiated",
            "Final quote approved and sent to customer",
            "Customer requested minor modifications to specifications",
            "Technical clarifications provided to customer",
            "Delivery timeline confirmed with production team"
        );

        for (int i = 0; i < random.nextInt(5) + 3; i++) { // 3-7 conversations per enquiry
            EnquiryConversation conversation = new EnquiryConversation();
            conversation.setEnquiry(enquiry);
            conversation.setMessage(messages.get(random.nextInt(messages.size())));
            conversation.setMessageType(getRandomMessageType());
            conversation.setDirection(getRandomMessageDirection());
            conversation.setSenderName(random.nextBoolean() ? "Sales Team" : "Customer Representative");
            conversation.setCreatedBy("sales_manager");

            enquiryConversationRepository.save(conversation);
        }
    }

    private void createSampleOrders() {
        List<Customer> customers = customerRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<Enquiry> enquiries = enquiryRepository.findAll();

        for (int i = 0; i < 15; i++) {
            Order order = new Order();
            order.setOrderNumber("ORD-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")) +
                               "-" + String.format("%04d", i + 3));
            order.setCustomer(customers.get(random.nextInt(customers.size())));

            // Some orders are converted from enquiries
            if (random.nextBoolean() && !enquiries.isEmpty()) {
                Enquiry enquiry = enquiries.get(random.nextInt(enquiries.size()));
                order.setEnquiry(enquiry);
                enquiry.setStatus(Enquiry.EnquiryStatus.CONVERTED);
                enquiryRepository.save(enquiry);
            }

            order.setDescription("Order for " + getRandomProjectType() + " system implementation");
            order.setRequirements("Complete system delivery with installation and commissioning");
            order.setStatus(getRandomOrderStatus());
            order.setPriority(getRandomOrderPriority());
            order.setPurchaseOrderNumber("PO-" + random.nextInt(99999) + 10000);

            LocalDateTime orderDate = LocalDateTime.now().minusDays(random.nextInt(60));
            order.setOrderDate(orderDate);
            order.setDeliveryExpectedDate(orderDate.plusDays(random.nextInt(90) + 30).toLocalDate());

            if (order.getStatus() == Order.OrderStatus.DISPATCHED || order.getStatus() == Order.OrderStatus.DELIVERED) {
                order.setDispatchDate(orderDate.plusDays(random.nextInt(45) + 15));
                order.setTransportDetails("Shipped via " + getRandomTransporter() + " with tracking number TRK" + random.nextInt(999999));
            }

            if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                order.setDeliveryActualDate(order.getDispatchDate().plusDays(random.nextInt(7) + 1));
            }

            order.setActive(true);
            order.setCreatedBy("sales_manager");
            order.setUpdatedBy("sales_manager");

            Order savedOrder = orderRepository.save(order);
            createOrderItems(savedOrder, products);
        }
    }

    private void createOrderItems(Order order, List<Product> products) {
        int itemCount = random.nextInt(3) + 1; // 1-3 items per order
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < itemCount; i++) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(products.get(random.nextInt(products.size())));
            item.setQuantity(random.nextInt(10) + 1);

            BigDecimal unitPrice = BigDecimal.valueOf(random.nextDouble() * 50000 + 10000)
                                           .setScale(2, BigDecimal.ROUND_HALF_UP);
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            item.setSpecifications("Production specifications for " + item.getProduct().getName());
            item.setNotes("Quality requirements and testing protocols defined");

            totalAmount = totalAmount.add(item.getTotalPrice());
            orderItemRepository.save(item);
        }

        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }

    // Helper methods for generating random data
    private String generatePhoneNumber() {
        return "+91-" + (7000000000L + random.nextInt(999999999));
    }

    private String generateCompanyEmail(String companyName) {
        return "info@" + companyName.toLowerCase().replaceAll("[^a-z0-9]", "") + ".com";
    }

    private String getRandomIndustry() {
        List<String> industries = Arrays.asList("automotive", "aerospace", "electronics", "pharmaceutical", "food processing");
        return industries.get(random.nextInt(industries.size()));
    }

    private String getRandomMaterial() {
        List<String> materials = Arrays.asList("precision components", "steel parts", "electronic assemblies", "mechanical parts");
        return materials.get(random.nextInt(materials.size()));
    }

    private String getRandomProductCategory() {
        List<String> categories = Arrays.asList("Automation", "Control Systems", "Measurement", "Safety", "Processing");
        return categories.get(random.nextInt(categories.size()));
    }

    private String getRandomProjectType() {
        List<String> projects = Arrays.asList("automation", "control system", "monitoring solution", "safety system");
        return projects.get(random.nextInt(projects.size()));
    }

    private String getRandomRequirement() {
        List<String> requirements = Arrays.asList("high precision assembly", "automated testing capability",
                                                "remote monitoring features", "safety compliance certification");
        return requirements.get(random.nextInt(requirements.size()));
    }

    private String getRandomTags() {
        List<String> tags = Arrays.asList("urgent,high-value", "standard,regular", "complex,technical", "simple,quick-delivery");
        return tags.get(random.nextInt(tags.size()));
    }

    private String getRandomTransporter() {
        List<String> transporters = Arrays.asList("DHL Express", "FedEx", "Blue Dart", "DTDC", "Professional Couriers");
        return transporters.get(random.nextInt(transporters.size()));
    }

    private Enquiry.EnquiryStatus getRandomEnquiryStatus() {
        Enquiry.EnquiryStatus[] statuses = Enquiry.EnquiryStatus.values();
        return statuses[random.nextInt(statuses.length)];
    }

    private Enquiry.EnquiryPriority getRandomEnquiryPriority() {
        Enquiry.EnquiryPriority[] priorities = Enquiry.EnquiryPriority.values();
        return priorities[random.nextInt(priorities.length)];
    }

    private Order.OrderStatus getRandomOrderStatus() {
        Order.OrderStatus[] statuses = Order.OrderStatus.values();
        return statuses[random.nextInt(statuses.length)];
    }

    private Order.OrderPriority getRandomOrderPriority() {
        Order.OrderPriority[] priorities = Order.OrderPriority.values();
        return priorities[random.nextInt(priorities.length)];
    }

    private EnquiryConversation.MessageType getRandomMessageType() {
        EnquiryConversation.MessageType[] types = EnquiryConversation.MessageType.values();
        return types[random.nextInt(types.length)];
    }

    private EnquiryConversation.MessageDirection getRandomMessageDirection() {
        EnquiryConversation.MessageDirection[] directions = EnquiryConversation.MessageDirection.values();
        return directions[random.nextInt(directions.length)];
    }
}
