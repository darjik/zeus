package org.zeus.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zeus.ims.entity.Product;
import org.zeus.ims.entity.ProductPart;
import org.zeus.ims.entity.User;
import org.zeus.ims.entity.UserRole;
import org.zeus.ims.entity.Vendor;
import org.zeus.ims.repository.ProductPartRepository;
import org.zeus.ims.repository.ProductRepository;
import org.zeus.ims.repository.UserRepository;
import org.zeus.ims.repository.VendorRepository;

import java.util.List;
import java.util.Random;

@Service
public class DataInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final ProductPartRepository productPartRepository;
    private final VendorRepository vendorRepository;

    @Autowired
    public DataInitializationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                     ProductRepository productRepository, ProductPartRepository productPartRepository,
                                     VendorRepository vendorRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.productPartRepository = productPartRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
        initializeDummyProducts();
    }

    private void initializeDefaultUsers() {
        if (userRepository.count() == 0) {
            createDefaultOwner();
            createSampleUsers();
        }
    }

    private void createDefaultOwner() {
        User owner = new User();
        owner.setUsername("admin");
        owner.setPassword(passwordEncoder.encode("admin123"));
        owner.setFullName("System Administrator");
        owner.setEmail("admin@zeus-ims.com");
        owner.setPhoneNumber("+1-234-567-8900");
        owner.setRole(UserRole.OWNER);
        owner.setActive(true);
        owner.setCreatedBy("system");

        userRepository.save(owner);
        System.out.println("Default owner created: admin/admin123");
    }

    private void createSampleUsers() {
        // Sales User
        User salesUser = new User();
        salesUser.setUsername("sales");
        salesUser.setPassword(passwordEncoder.encode("sales123"));
        salesUser.setFullName("Sales Manager");
        salesUser.setEmail("sales@zeus-ims.com");
        salesUser.setPhoneNumber("+1-234-567-8901");
        salesUser.setRole(UserRole.SALES);
        salesUser.setActive(true);
        salesUser.setCreatedBy("system");
        userRepository.save(salesUser);

        // Production Manager
        User productionUser = new User();
        productionUser.setUsername("production");
        productionUser.setPassword(passwordEncoder.encode("production123"));
        productionUser.setFullName("Production Manager");
        productionUser.setEmail("production@zeus-ims.com");
        productionUser.setPhoneNumber("+1-234-567-8902");
        productionUser.setRole(UserRole.PRODUCTION_MANAGER);
        productionUser.setActive(true);
        productionUser.setCreatedBy("system");
        userRepository.save(productionUser);

        // Workshop Personnel
        User workshopUser = new User();
        workshopUser.setUsername("workshop");
        workshopUser.setPassword(passwordEncoder.encode("workshop123"));
        workshopUser.setFullName("Workshop Supervisor");
        workshopUser.setEmail("workshop@zeus-ims.com");
        workshopUser.setPhoneNumber("+1-234-567-8903");
        workshopUser.setRole(UserRole.WORKSHOP_PERSONNEL);
        workshopUser.setActive(true);
        workshopUser.setCreatedBy("system");
        userRepository.save(workshopUser);

        // Accountant
        User accountantUser = new User();
        accountantUser.setUsername("accountant");
        accountantUser.setPassword(passwordEncoder.encode("accountant123"));
        accountantUser.setFullName("Chief Accountant");
        accountantUser.setEmail("accountant@zeus-ims.com");
        accountantUser.setPhoneNumber("+1-234-567-8904");
        accountantUser.setRole(UserRole.ACCOUNTANT);
        accountantUser.setActive(true);
        accountantUser.setCreatedBy("system");
        userRepository.save(accountantUser);

        System.out.println("Sample users created successfully!");
    }

    private void initializeDummyProducts() {
        if (productRepository.count() == 0) {
            createSampleProducts();
        }
    }

    private void createSampleProducts() {
        List<Vendor> vendors = vendorRepository.findAll();
        Random random = new Random();

        // Product 1: Industrial Conveyor Belt System
        Product conveyor = Product.builder()
                .name("Heavy Duty Conveyor Belt System")
                .description("Industrial grade conveyor belt system designed for heavy-duty material handling in manufacturing environments. Features steel frame construction with variable speed control.")
                .modelNumber("CBX-3000")
                .brand("IndustryMax")
                .category("Material Handling")
                .unitOfMeasure("Sets")
                .weight(850.0)
                .dimensions("3000mm x 800mm x 1200mm")
                .material("Steel Frame with Rubber Belt")
                .specifications("Load capacity: 500kg/m, Speed: 0.1-2.0 m/s, Motor: 5.5kW, Belt width: 800mm")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(conveyor);

        // Product 2: Precision CNC Milling Machine
        Product cncMachine = Product.builder()
                .name("Precision CNC Vertical Milling Machine")
                .description("High-precision 3-axis CNC milling machine with automatic tool changer. Suitable for precision machining of metal components with tight tolerances.")
                .modelNumber("VM-850")
                .brand("TechPrecision")
                .category("Machine Tools")
                .unitOfMeasure("Pieces")
                .weight(2500.0)
                .dimensions("2200mm x 1800mm x 2400mm")
                .material("Cast Iron Base with Steel Framework")
                .specifications("Spindle speed: 8000 RPM, Tool capacity: 20, Working area: 850x450x400mm, Positioning accuracy: ±0.005mm")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(cncMachine);

        // Product 3: Hydraulic Press
        Product hydraulicPress = Product.builder()
                .name("Industrial Hydraulic Press")
                .description("Heavy-duty hydraulic press for metal forming, stamping, and pressing operations. Features programmable pressure control and safety systems.")
                .modelNumber("HP-200T")
                .brand("PowerForge")
                .category("Forming Equipment")
                .unitOfMeasure("Pieces")
                .weight(3200.0)
                .dimensions("2500mm x 1500mm x 3500mm")
                .material("Steel Frame with Hydraulic Components")
                .specifications("Max pressure: 200 tons, Stroke: 400mm, Table size: 1200x800mm, Pump motor: 11kW")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(hydraulicPress);

        // Product 4: Industrial Air Compressor
        Product airCompressor = Product.builder()
                .name("Rotary Screw Air Compressor")
                .description("Energy-efficient rotary screw air compressor with variable speed drive. Designed for continuous operation in industrial environments.")
                .modelNumber("RS-75VSD")
                .brand("AirFlow")
                .category("Pneumatic Equipment")
                .unitOfMeasure("Pieces")
                .weight(1200.0)
                .dimensions("1800mm x 1200mm x 1600mm")
                .material("Steel Housing with Cast Iron Compressor Block")
                .specifications("Flow rate: 13.2 m³/min, Pressure: 8 bar, Motor: 75kW VSD, Noise level: <70dB")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(airCompressor);

        // Product 5: Welding Robot System
        Product weldingRobot = Product.builder()
                .name("6-Axis Welding Robot System")
                .description("Automated welding robot with 6-axis articulation for complex welding applications. Includes welding power source and wire feeder.")
                .modelNumber("WR-1400")
                .brand("RoboWeld")
                .category("Automation Equipment")
                .unitOfMeasure("Sets")
                .weight(750.0)
                .dimensions("1400mm reach, Base: 600mm x 600mm")
                .material("Aluminum Alloy Arms with Steel Base")
                .specifications("Payload: 6kg, Reach: 1400mm, Repeatability: ±0.08mm, Welding current: 350A")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(weldingRobot);

        // Product 6: Overhead Crane System
        Product overheadCrane = Product.builder()
                .name("Bridge Overhead Crane")
                .description("Double girder overhead crane for heavy lifting applications in industrial facilities. Remote control operation with safety features.")
                .modelNumber("BOC-20T")
                .brand("LiftMaster")
                .category("Lifting Equipment")
                .unitOfMeasure("Sets")
                .weight(8500.0)
                .dimensions("Span: 20m, Lift height: 12m")
                .material("Steel Structure with Wire Rope Hoist")
                .specifications("Capacity: 20 tons, Span: 20m, Lift height: 12m, Speed: 8/2 m/min")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(overheadCrane);

        // Product 7: Industrial Lathe Machine
        Product lathe = Product.builder()
                .name("Heavy Duty CNC Lathe")
                .description("Computer-controlled lathe machine for precision turning operations on large diameter workpieces. Suitable for automotive and aerospace industries.")
                .modelNumber("CL-800")
                .brand("TurnTech")
                .category("Machine Tools")
                .unitOfMeasure("Pieces")
                .weight(4200.0)
                .dimensions("4500mm x 2200mm x 2000mm")
                .material("Cast Iron Bed with Steel Components")
                .specifications("Swing over bed: 800mm, Distance between centers: 3000mm, Spindle speed: 2000 RPM, Chuck: 315mm")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(lathe);

        // Product 8: Industrial Furnace
        Product furnace = Product.builder()
                .name("Electric Heat Treatment Furnace")
                .description("High-temperature electric furnace for heat treatment, annealing, and tempering of metal components. Programmable temperature control.")
                .modelNumber("EHF-1200")
                .brand("HeatPro")
                .category("Heat Treatment")
                .unitOfMeasure("Pieces")
                .weight(2800.0)
                .dimensions("2000mm x 1500mm x 1800mm")
                .material("Refractory Lined Steel Chamber")
                .specifications("Max temperature: 1200°C, Chamber: 600x400x400mm, Heating elements: Silicon Carbide, Power: 45kW")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(furnace);

        // Product 9: Packaging Machine
        Product packagingMachine = Product.builder()
                .name("Automatic Packaging Machine")
                .description("High-speed automatic packaging machine for industrial products. Features form-fill-seal technology with quality control systems.")
                .modelNumber("APM-500")
                .brand("PackTech")
                .category("Packaging Equipment")
                .unitOfMeasure("Pieces")
                .weight(1800.0)
                .dimensions("3500mm x 1200mm x 2200mm")
                .material("Stainless Steel Frame with Food Grade Components")
                .specifications("Speed: 500 packs/min, Bag width: 50-200mm, Film width: 350mm, Power: 8.5kW")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(packagingMachine);

        // Product 10: Industrial Pump System
        Product pumpSystem = Product.builder()
                .name("Centrifugal Pump System")
                .description("Heavy-duty centrifugal pump system for industrial fluid transfer applications. Corrosion-resistant materials for chemical compatibility.")
                .modelNumber("CP-300")
                .brand("FlowMax")
                .category("Fluid Handling")
                .unitOfMeasure("Sets")
                .weight(450.0)
                .dimensions("1200mm x 800mm x 900mm")
                .material("Stainless Steel Impeller with Cast Iron Casing")
                .specifications("Flow rate: 300 m³/h, Head: 85m, Suction: 200mm, Discharge: 150mm, Motor: 30kW")
                .active(true)
                .createdBy("system")
                .updatedBy("system")
                .build();
        productRepository.save(pumpSystem);

        System.out.println("10 sample products created successfully!");

        // Create parts for each product
        createProductParts(conveyor, vendors, random);
        createProductParts(cncMachine, vendors, random);
        createProductParts(hydraulicPress, vendors, random);
        createProductParts(airCompressor, vendors, random);
        createProductParts(weldingRobot, vendors, random);
        createProductParts(overheadCrane, vendors, random);
        createProductParts(lathe, vendors, random);
        createProductParts(furnace, vendors, random);
        createProductParts(packagingMachine, vendors, random);
        createProductParts(pumpSystem, vendors, random);

        System.out.println("Product parts created successfully!");
    }

    private void createProductParts(Product product, List<Vendor> vendors, Random random) {
        String[][] partsData = getPartsDataForProduct(product.getName());

        for (String[] partData : partsData) {
            Vendor randomVendor = vendors.isEmpty() ? null : vendors.get(random.nextInt(vendors.size()));

            ProductPart part = ProductPart.builder()
                    .partName(partData[0])
                    .partNumber(partData[1])
                    .description(partData[2])
                    .material(partData[3])
                    .dimensions(partData[4])
                    .unitOfMeasure(partData[5])
                    .quantityRequired(Integer.parseInt(partData[6]))
                    .specifications(partData[7])
                    .active(true)
                    .product(product)
                    .vendor(randomVendor)
                    .createdBy("system")
                    .updatedBy("system")
                    .build();

            productPartRepository.save(part);
        }
    }

    private String[][] getPartsDataForProduct(String productName) {
        switch (productName) {
            case "Heavy Duty Conveyor Belt System":
                return new String[][]{
                    {"Conveyor Belt", "CB-3000-RB", "Heavy duty rubber conveyor belt", "Rubber with Steel Cord", "3000mm x 800mm x 15mm", "Meters", "1", "Temperature range: -40°C to +120°C"},
                    {"Drive Motor", "DM-5.5KW", "Variable speed drive motor", "Cast Iron", "400mm x 300mm x 350mm", "Pieces", "1", "Power: 5.5kW, Speed: 1450 RPM, IP55 protection"},
                    {"Drive Pulley", "DP-800-ST", "Steel drive pulley with lagging", "Steel with Rubber Lagging", "800mm diameter x 800mm width", "Pieces", "1", "Shaft diameter: 80mm, Dynamic balancing grade G6.3"},
                    {"Idler Roller", "IR-159-ST", "Steel idler roller", "Steel", "159mm diameter x 800mm length", "Pieces", "12", "Bearing: 6204-2RS, Load capacity: 1000N"},
                    {"Belt Tensioning Device", "BTD-HYD", "Hydraulic belt tensioning system", "Steel", "500mm x 300mm x 200mm", "Pieces", "1", "Hydraulic pressure: 50 bar, Stroke: 200mm"},
                    {"Support Frame", "SF-ST-3000", "Steel support frame structure", "Structural Steel", "3000mm x 800mm x 1200mm", "Sets", "1", "Material: S235JR, Welded construction, Powder coated"},
                    {"Control Panel", "CP-VFD", "Variable frequency drive control panel", "Steel Enclosure", "600mm x 400mm x 200mm", "Pieces", "1", "IP54 protection, Emergency stop, Speed control"},
                    {"Safety Guards", "SG-MESH", "Mesh safety guards", "Steel Mesh", "Various sizes", "Sets", "1", "Material: Galvanized steel, Mesh size: 20x20mm"},
                    {"Bearing Housing", "BH-80-CI", "Cast iron bearing housing", "Cast Iron", "200mm x 150mm x 100mm", "Pieces", "4", "Bearing size: 80mm, Grease lubrication, Labyrinth seal"},
                    {"Emergency Stop System", "ESS-ROPE", "Emergency stop rope switch", "Plastic Housing", "3000mm length", "Sets", "1", "IP67 protection, Normally closed contacts"}
                };
            case "Precision CNC Vertical Milling Machine":
                return new String[][]{
                    {"Spindle Motor", "SM-8KW-HSK", "High-speed spindle motor", "Steel", "300mm x 250mm x 400mm", "Pieces", "1", "Power: 8kW, Speed: 8000 RPM, HSK-A63 interface"},
                    {"Ball Screw", "BS-2505", "Precision ball screw for X-axis", "Hardened Steel", "1200mm length x 25mm diameter", "Pieces", "3", "Lead: 5mm, Accuracy grade: C3, Preloaded"},
                    {"Linear Guide", "LG-25-1200", "Linear guide rail and carriage", "Hardened Steel", "1200mm length", "Sets", "3", "Size: 25mm, Load rating: 15kN, Accuracy: ±0.005mm"},
                    {"Tool Changer", "TC-20-ATC", "Automatic tool changer", "Steel", "800mm x 600mm x 400mm", "Pieces", "1", "Capacity: 20 tools, Change time: 3.5 seconds, HSK-A63"},
                    {"CNC Controller", "CNC-CTRL", "Computer numerical control system", "Electronic", "400mm x 300mm x 150mm", "Pieces", "1", "3-axis control, 1MB program memory, Ethernet interface"},
                    {"Machine Bed", "MB-CI-850", "Cast iron machine bed", "Cast Iron", "2200mm x 1800mm x 400mm", "Pieces", "1", "Material: FC250, Stress relieved, Precision ground"},
                    {"Coolant System", "CS-FLOOD", "Flood coolant system", "Stainless Steel", "400mm x 300mm x 500mm", "Sets", "1", "Flow rate: 50 L/min, Pressure: 5 bar, Filtration"},
                    {"Way Covers", "WC-TELES", "Telescopic way covers", "Steel", "Various sizes", "Sets", "3", "Type: Telescopic, Material: Steel laminate, Oil resistant"},
                    {"Servo Motor", "SV-2KW", "AC servo motor for axis drive", "Aluminum", "200mm x 150mm x 300mm", "Pieces", "3", "Power: 2kW, Torque: 9.5 Nm, Encoder: 2500 ppr"},
                    {"Chip Conveyor", "CC-HINGE", "Hinged belt chip conveyor", "Steel", "2000mm length", "Pieces", "1", "Belt width: 150mm, Capacity: 50 L/min, Coolant proof"}
                };
            case "Industrial Hydraulic Press":
                return new String[][]{
                    {"Hydraulic Cylinder", "HC-200T", "Main hydraulic cylinder", "Hardened Steel", "400mm bore x 400mm stroke", "Pieces", "1", "Pressure: 200 bar, Chrome plated rod, Cushioning"},
                    {"Hydraulic Pump", "HP-11KW", "Variable displacement pump", "Cast Iron", "500mm x 400mm x 300mm", "Pieces", "1", "Flow: 75 L/min, Pressure: 200 bar, Load sensing"},
                    {"Press Frame", "PF-ST-200T", "Heavy duty steel frame", "Structural Steel", "2500mm x 1500mm x 3500mm", "Pieces", "1", "Material: S355, Welded construction, Stress relieved"},
                    {"Hydraulic Valves", "HV-PROP", "Proportional directional valve", "Steel", "150mm x 100mm x 80mm", "Pieces", "4", "Flow: 80 L/min, Pressure: 250 bar, Proportional control"},
                    {"Control System", "CS-PLC", "PLC control system", "Electronic", "600mm x 400mm x 200mm", "Pieces", "1", "Touch screen HMI, Safety circuits, Data logging"},
                    {"Safety System", "SS-LIGHT", "Light curtain safety system", "Aluminum", "2000mm height", "Sets", "1", "Type 4 safety, 14mm resolution, Muting function"},
                    {"Hydraulic Tank", "HT-300L", "Hydraulic oil reservoir", "Steel", "1200mm x 800mm x 400mm", "Pieces", "1", "Capacity: 300L, Return filter, Breather, Level gauge"},
                    {"Press Table", "PT-1200x800", "Adjustable press table", "Steel", "1200mm x 800mm x 50mm", "Pieces", "1", "Material: Tool steel, Ground surface, T-slots"},
                    {"Position Sensor", "PS-LINEAR", "Linear position sensor", "Stainless Steel", "500mm stroke", "Pieces", "1", "Type: Magnetostrictive, Accuracy: ±0.1mm, Analog output"},
                    {"Accumulator", "AC-40L", "Hydraulic accumulator", "Steel", "400mm diameter x 600mm height", "Pieces", "1", "Volume: 40L, Pressure: 250 bar, Bladder type"}
                };
            case "Rotary Screw Air Compressor":
                return new String[][]{
                    {"Air Filter", "AF-10", "10 Micron air filter element", "Polyester", "250mm x 150mm", "Pieces", "1", "Filtration efficiency: 99.9%"},
                    {"Oil Separator", "OS-75", "Oil separator for rotary screw compressor", "Steel", "300mm x 200mm", "Pieces", "1", "Separation efficiency: 99.9%"},
                    {"Belt Guard", "BG-75", "Belt guard for compressor drive belt", "Steel", "600mm x 400mm", "Pieces", "1", "Safety guard for drive belt"},
                    {"V-Belt", "VB-75", "Replacement V-belt for compressor", "Rubber", "Length: 1500mm", "Pieces", "1", "High strength, Oil resistant"},
                    {"Compressor Oil", "CO-20L", "20L drum of rotary screw compressor oil", "Mineral Oil", "Drum: 400mm x 600mm", "Drums", "1", "ISO VG 68, Anti-wear, Anti-oxidation"},
                    {"Temperature Sensor", "TS-PT100", "PT100 temperature sensor", "Stainless Steel", "Length: 100mm", "Pieces", "1", "Temperature range: -50°C to +150°C"},
                    {"Pressure Sensor", "PS-4-20MA", "4-20mA pressure sensor", "Stainless Steel", "Length: 100mm", "Pieces", "1", "Pressure range: 0-10 bar"},
                    {"Control Relay", "CR-24DC", "24V DC control relay", "Plastic", "100mm x 80mm", "Pieces", "1", "Coil voltage: 24V DC, Contact rating: 10A"},
                    {"LED Indicator", "LED-GREEN", "Green LED indicator light", "Plastic", "Diameter: 22mm", "Pieces", "1", "Voltage: 24V DC, Current: 20mA"},
                    {"Hose Kit", "HK-75", "Compressed air hose kit", "Rubber", "Length: 10m", "Sets", "1", "Working pressure: 10 bar, Burst pressure: 30 bar"}
                };
            case "6-Axis Welding Robot System":
                return new String[][]{
                    {"Welding Torch", "WT-500A", "500A air-cooled welding torch", "Copper", "Length: 400mm", "Pieces", "1", "Max current: 500A, Voltage: 10.5-15V"},
                    {"Robot Controller", "RC-6AXIS", "6-axis robot controller", "Electronic", "600mm x 400mm x 200mm", "Pieces", "1", "6-axis control, Teach pendant, Ethernet"},
                    {"Wire Feeder", "WF-1.0MM", "1.0mm solid wire feeder", "Steel", "300mm x 250mm x 150mm", "Pieces", "1", "Wire diameter: 0.8-1.0mm, Speed: 0-12m/min"},
                    {"Gas Diffuser", "GD-500", "Gas diffuser for welding torch", "Brass", "Diameter: 50mm", "Pieces", "1", "For 500A torch, Gas flow: 10-15L/min"},
                    {"Contact Tip", "CT-1.0", "1.0mm contact tip", "Copper", "Diameter: 10mm", "Pieces", "1", "For solid wire, Thread: M6"},
                    {"Nozzle", "NZ-50", "Welding nozzle for torch", "Copper", "Diameter: 50mm", "Pieces", "1", "For 500A torch, Length: 100mm"},
                    {"Collet Body", "CB-35", "35mm collet body for torch", "Brass", "Length: 50mm", "Pieces", "1", "For 500A torch, Thread: M10"},
                    {"Electrode", "EL-2.4", "2.4mm tungsten electrode", "Tungsten", "Length: 175mm", "Pieces", "1", "For DC welding, Color code: Green"},
                    {"Fume Extractor", "FE-100", "Fume extractor for welding area", "Steel", "300mm x 300mm x 600mm", "Pieces", "1", "Extraction rate: 1000m³/h, Filter: HEPA"},
                    {"Welding Curtain", "WC-2X3", "Welding curtain 2x3m", "PVC", "2m x 3m", "Pieces", "1", "Flame retardant, Weld spatter resistant"}
                };
            case "Bridge Overhead Crane":
                return new String[][]{
                    {"Hoist Unit", "HU-20T", "20 ton electric hoist unit", "Steel", "Length: 3000mm", "Sets", "1", "Capacity: 20 tons, Lift height: 12m"},
                    {"Bridge Girder", "BG-20T", "Double girder for overhead crane", "Steel", "Span: 20m", "Sets", "1", "Material: S355, Welded construction"},
                    {"End Truck", "ET-20T", "End truck assembly for overhead crane", "Steel", "Width: 3000mm", "Sets", "1", "Includes wheels, bearings, and frame"},
                    {"Control Pendant", "CP-20T", "Control pendant for crane operation", "Plastic", "Length: 500mm", "Pieces", "1", "Push buttons for up/down, emergency stop"},
                    {"Limit Switch", "LS-20T", "Limit switch for hoist travel", "Steel", "Length: 200mm", "Pieces", "1", "Mechanical limit switch, IP67 protection"},
                    {"Safety Hook", "SH-20T", "Safety hook for hoisting", "Steel", "Diameter: 30mm", "Pieces", "1", "With safety latch, Capacity: 20 tons"},
                    {"Wire Rope", "WR-20T", "20mm wire rope for hoisting", "Steel", "Length: 50m", "Pieces", "1", "Breaking strength: 80 tons, Construction: 6x37"},
                    {"Sway Bracket", "SB-20T", "Sway control bracket", "Steel", "Length: 1000mm", "Pieces", "1", "For sway prevention, Adjustable"},
                    {"Crane Rail", "CR-20T", "Crane rail for overhead crane", "Steel", "Length: 12m", "Pieces", "2", "Profile: I-beam, Height: 200mm"},
                    {"Rail Support", "RS-20T", "Rail support for overhead crane", "Steel", "Height: 300mm", "Pieces", "2", "With adjustable base, Hot-dip galvanized"}
                };
            case "Heavy Duty CNC Lathe":
                return new String[][]{
                    {"Chuck", "C-315", "315mm hydraulic chuck", "Steel", "Diameter: 315mm", "Pieces", "1", "With jaws, For cylindrical workpieces"},
                    {"Tailstock", "TS-800", "Tailstock with quill", "Cast Iron", "Length: 800mm", "Pieces", "1", "Quill stroke: 150mm, Morse taper: MT4"},
                    {"Tool Post", "TP-4WAY", "4-way tool post", "Cast Iron", "Length: 200mm", "Pieces", "1", "For quick tool change, With T-nuts"},
                    {"Carriage", "CARR-800", "Carriage assembly", "Cast Iron", "Length: 800mm", "Pieces", "1", "With lead screw and nut, Precision ground"},
                    {"Apron", "APR-800", "Apron assembly for carriage", "Cast Iron", "Length: 800mm", "Pieces", "1", "With handwheel and controls"},
                    {"Bed", "BED-CI", "Cast iron bed", "Cast Iron", "Length: 4500mm", "Pieces", "1", "Precision ground, With T-slots"},
                    {"Coolant Pump", "CP-220V", "220V coolant pump", "Stainless Steel", "Height: 150mm", "Pieces", "1", "Flow rate: 20 L/min, Pressure: 3 bar"},
                    {"Chip Pan", "CP-800", "Chip pan for lathe", "Steel", "Length: 800mm", "Pieces", "1", "With drainage, Removable"},
                    {"LED Work Light", "LWL-10W", "10W LED work light", "Aluminum", "Length: 200mm", "Pieces", "1", "With magnetic base, 360° rotation"},
                    {"Emergency Stop Button", "ESB-22MM", "22mm emergency stop button", "Plastic", "Diameter: 22mm", "Pieces", "1", "Push to stop, Pull to reset"}
                };
            case "Electric Heat Treatment Furnace":
                return new String[][]{
                    {"Heating Element", "HE-SIC-45KW", "Silicon carbide heating element", "Silicon Carbide", "Length: 600mm", "Pieces", "6", "Max temperature: 1200°C, Power: 45kW"},
                    {"Thermocouple", "TC-K-500", "K-type thermocouple", "Nickel-Chromium", "Length: 100mm", "Pieces", "2", "Temperature range: 0-500°C, Accuracy: ±2%"},
                    {"Control Module", "CM-1200", "Temperature control module", "Electronic", "300mm x 200mm x 100mm", "Pieces", "1", "PID control, SSR output, RS485"},
                    {"Safety Relay", "SR-2NO", "Safety relay with 2 NO contacts", "Plastic", "Length: 100mm", "Pieces", "1", "For safety interlock, 24V DC"},
                    {"Door Switch", "DS-1NC", "Normally closed door switch", "Plastic", "Length: 50mm", "Pieces", "1", "For door safety interlock"},
                    {"Fuse Holder", "FH-10X38", "Fuse holder for 10x38mm fuses", "Ceramic", "Length: 50mm", "Pieces", "1", "With indicator light, 10A"},
                    {"Power Cable", "PC-5G6", "5-core power cable 6mm²", "Copper", "Length: 10m", "Pieces", "1", "For 3-phase connection, H07RN-F"},
                    {"Insulation Mat", "IM-1200", "Insulation mat for furnace", "Ceramic Fiber", "1200mm x 800mm", "Pieces", "1", "Thickness: 25mm, Temp. resistance: 1260°C"},
                    {"Thermal Camera", "TC-IR", "Infrared thermal camera", "Electronic", "Width: 100mm", "Pieces", "1", "For non-contact temperature measurement"},
                    {"Emergency Stop Switch", "ESS-22MM", "22mm emergency stop switch", "Plastic", "Diameter: 22mm", "Pieces", "1", "Push to stop, Pull to reset"}
                };
            case "Automatic Packaging Machine":
                return new String[][]{
                    {"Forming Tube", "FT-50-200", "Forming tube for packaging machine", "Stainless Steel", "Length: 200mm", "Pieces", "1", "For bag forming, Diameter: 50-200mm"},
                    {"Filling Nozzle", "FN-10", "10L filling nozzle", "Stainless Steel", "Length: 300mm", "Pieces", "1", "For liquid and granule filling"},
                    {"Sealing Jaw", "SJ-200", "Horizontal sealing jaw", "Aluminum", "Length: 250mm", "Pieces", "1", "For heat sealing of bags"},
                    {"Cooling Tunnel", "CT-2M", "Cooling tunnel for packaged products", "Stainless Steel", "Length: 2000mm", "Pieces", "1", "With cooling fans, Adjustable speed"},
                    {"Conveyor Belt", "CB-350", "Conveyor belt for packaging line", "Rubber", "Length: 350mm", "Pieces", "1", "With adjustable speed drive"},
                    {"Control Panel", "CP-PLC", "PLC control panel for packaging machine", "Steel", "Width: 800mm", "Pieces", "1", "With touch screen, Emergency stop"},
                    {"Sensor Kit", "SK-PACK", "Sensor kit for packaging machine", "Electronic", "Width: 100mm", "Pieces", "1", "Includes photoelectric sensors and cables"},
                    {"Power Supply", "PS-24V", "24V power supply for control system", "Metal", "Width: 200mm", "Pieces", "1", "Input: 100-240VAC, Output: 24VDC"},
                    {"Foot Pedal", "FP-1", "Foot pedal switch for manual control", "Plastic", "Length: 150mm", "Pieces", "1", "For manual start/stop of the machine"},
                    {"Emergency Stop Button", "ESB-22MM", "22mm emergency stop button", "Plastic", "Diameter: 22mm", "Pieces", "1", "Push to stop, Pull to reset"}
                };
            case "Centrifugal Pump System":
                return new String[][]{
                    {"Impeller", "IP-300", "Cast iron impeller", "Cast Iron", "Diameter: 300mm", "Pieces", "1", "For centrifugal force generation"},
                    {"Pump Housing", "PH-300", "Pump housing in two parts", "Cast Iron", "Length: 600mm", "Pieces", "1", "With flange connections"},
                    {"Suction Strainer", "SS-200", "Suction strainer for pump", "Stainless Steel", "Length: 200mm", "Pieces", "1", "Mesh size: 2mm, Easy to clean"},
                    {"Discharge Elbow", "DE-150", "Discharge elbow for pump", "Cast Iron", "Angle: 90°, Length: 150mm", "Pieces", "1", "With flange connections"},
                    {"Mechanical Seal", "MS-300", "Mechanical seal for pump shaft", "Ceramic/Carbon", "Diameter: 50mm", "Pieces", "1", "For sealing pump shaft, Prevents leakage"},
                    {"O-ring Kit", "OK-300", "O-ring kit for pump housing", "Nitrile Rubber", "Various sizes", "Sets", "1", "For sealing pump housing joints"},
                    {"Mounting Base", "MB-300", "Mounting base for pump", "Steel", "Length: 400mm", "Pieces", "1", "With vibration dampers"},
                    {"Motor Mount", "MM-300", "Motor mount for pump drive", "Steel", "Length: 300mm", "Pieces", "1", "With adjustable height"},
                    {"V-belt Pulley", "VP-300", "V-belt pulley for motor", "Cast Iron", "Diameter: 300mm", "Pieces", "1", "For V-belt drive, Keyway: 10mm"},
                    {"Emergency Stop Button", "ESB-22MM", "22mm emergency stop button", "Plastic", "Diameter: 22mm", "Pieces", "1", "Push to stop, Pull to reset"}
                };
            default:
                return new String[][]{
                    {"Generic Part 1", "GP-001", "Standard component part", "Steel", "100mm x 50mm x 25mm", "Pieces", "1", "Standard specifications"},
                    {"Generic Part 2", "GP-002", "Standard component part", "Aluminum", "150mm x 75mm x 30mm", "Pieces", "2", "Standard specifications"},
                    {"Generic Part 3", "GP-003", "Standard component part", "Plastic", "80mm x 40mm x 20mm", "Pieces", "4", "Standard specifications"}
                };
        }
    }
}
