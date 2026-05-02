package com.complaintiq.config;
import com.complaintiq.agent.Agent;
import com.complaintiq.agent.AgentRepository;
import com.complaintiq.agent.enums.AgentRole;
import com.complaintiq.ai.*;
import com.complaintiq.ai.enums.*;
import com.complaintiq.assignment.*;
import com.complaintiq.assignment.enums.AssignmentStatus;
import com.complaintiq.auth.*;
import com.complaintiq.auth.enums.UserRole;
import com.complaintiq.common.TicketIdGenerator;
import com.complaintiq.complaint.*;
import com.complaintiq.complaint.enums.*;
import com.complaintiq.customer.Customer;
import com.complaintiq.customer.CustomerRepository;
import com.complaintiq.customer.enums.CustomerTier;
import com.complaintiq.department.Department;
import com.complaintiq.department.DepartmentRepository;
import com.complaintiq.resolution.*;
import com.complaintiq.resolution.enums.ResolutionType;
import com.complaintiq.sla.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Slf4j @Component @Profile("dev") @RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final AppUserRepository appUserRepository;
    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintRepository complaintRepository;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final AssignmentRepository assignmentRepository;
    private final ResolutionRepository resolutionRepository;
    private final SLAConfigRepository slaConfigRepository;
    private final ComplaintActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;
    private final TicketIdGenerator ticketIdGenerator;

    @Override @Transactional
    public void run(String... args) {
        if (appUserRepository.count() > 0) {
            log.info("DataSeeder skipped — data already exists.");
            complaintRepository.findMaxTicketSequence().ifPresent(ticketIdGenerator::initSequence);
            return;
        }
        log.info("DataSeeder starting...");
        seedSLAConfigs();
        List<Department> departments = seedDepartments();
        List<Agent> agents = seedAgents(departments);
        updateDepartmentHeads(departments, agents);
        seedAdminUsers();
        List<Customer> customers = seedCustomers();
        seedComplaints(customers, agents, departments);
        log.info("DataSeeder complete.");
    }

    private void seedSLAConfigs() {
        if (slaConfigRepository.count() > 0) return;
        slaConfigRepository.saveAll(List.of(
                SLAConfig.builder().urgencyLevel(UrgencyLevel.CRITICAL).resolutionHours(2).warningThresholdPercent(75).escalationChain("JUNIOR,SENIOR,TEAM_LEAD,MANAGER").build(),
                SLAConfig.builder().urgencyLevel(UrgencyLevel.HIGH).resolutionHours(6).warningThresholdPercent(75).escalationChain("JUNIOR,SENIOR,TEAM_LEAD,MANAGER").build(),
                SLAConfig.builder().urgencyLevel(UrgencyLevel.MEDIUM).resolutionHours(24).warningThresholdPercent(75).escalationChain("JUNIOR,SENIOR,TEAM_LEAD,MANAGER").build(),
                SLAConfig.builder().urgencyLevel(UrgencyLevel.LOW).resolutionHours(72).warningThresholdPercent(75).escalationChain("JUNIOR,SENIOR,TEAM_LEAD,MANAGER").build()
        ));
        log.info("SLA configs seeded: 4 configs");
    }

    private List<Department> seedDepartments() {
        if (departmentRepository.count() > 0) return departmentRepository.findAll();
        List<Department> departments = departmentRepository.saveAll(List.of(
                Department.builder().name("Delivery & Logistics").category(ComplaintCategory.DELIVERY).description("Handles all delivery and shipping related complaints").build(),
                Department.builder().name("Payments & Billing").category(ComplaintCategory.PAYMENT).description("Handles payment failures and billing disputes").build(),
                Department.builder().name("Product Quality").category(ComplaintCategory.PRODUCT).description("Handles defective products and quality complaints").build(),
                Department.builder().name("Refunds & Returns").category(ComplaintCategory.REFUND).description("Handles refund requests and return processing").build(),
                Department.builder().name("Technical Support").category(ComplaintCategory.TECHNICAL).description("Handles app, website and technical issues").build()
        ));
        log.info("Departments seeded: {}", departments.size());
        return departments;
    }

    private List<Agent> seedAgents(List<Department> departments) {
        if (agentRepository.count() > 0) return agentRepository.findAll();
        Department delivery = departments.get(0); Department payment = departments.get(1);
        Department product = departments.get(2); Department refund = departments.get(3); Department technical = departments.get(4);
        String defaultPassword = passwordEncoder.encode("Agent@123");
        List<Agent> agents = agentRepository.saveAll(List.of(
                Agent.builder().name("Priya Sharma").email("priya.sharma@complaintiq.com").phone("+91-9876543210").department(delivery).role(AgentRole.TEAM_LEAD).isAvailable(true).currentLoad(2).maxLoad(15).totalResolved(87).avgResolutionTimeHours(3.2).build(),
                Agent.builder().name("Rahul Verma").email("rahul.verma@complaintiq.com").phone("+91-9876543211").department(delivery).role(AgentRole.JUNIOR).isAvailable(true).currentLoad(4).maxLoad(10).totalResolved(34).avgResolutionTimeHours(5.8).build(),
                Agent.builder().name("Ananya Patel").email("ananya.patel@complaintiq.com").phone("+91-9876543212").department(payment).role(AgentRole.SENIOR).isAvailable(true).currentLoad(3).maxLoad(12).totalResolved(112).avgResolutionTimeHours(2.9).build(),
                Agent.builder().name("Vikram Singh").email("vikram.singh@complaintiq.com").phone("+91-9876543213").department(payment).role(AgentRole.JUNIOR).isAvailable(true).currentLoad(6).maxLoad(10).totalResolved(28).avgResolutionTimeHours(7.1).build(),
                Agent.builder().name("Sneha Reddy").email("sneha.reddy@complaintiq.com").phone("+91-9876543214").department(product).role(AgentRole.SENIOR).isAvailable(true).currentLoad(1).maxLoad(12).totalResolved(95).avgResolutionTimeHours(4.1).build(),
                Agent.builder().name("Arjun Mehta").email("arjun.mehta@complaintiq.com").phone("+91-9876543215").department(refund).role(AgentRole.TEAM_LEAD).isAvailable(true).currentLoad(3).maxLoad(15).totalResolved(143).avgResolutionTimeHours(2.4).build(),
                Agent.builder().name("Kavya Nair").email("kavya.nair@complaintiq.com").phone("+91-9876543216").department(refund).role(AgentRole.JUNIOR).isAvailable(false).currentLoad(0).maxLoad(10).totalResolved(19).avgResolutionTimeHours(8.3).build(),
                Agent.builder().name("Rohan Joshi").email("rohan.joshi@complaintiq.com").phone("+91-9876543217").department(technical).role(AgentRole.SENIOR).isAvailable(true).currentLoad(2).maxLoad(12).totalResolved(76).avgResolutionTimeHours(3.7).build(),
                Agent.builder().name("Deepak Kumar").email("deepak.kumar@complaintiq.com").phone("+91-9876543218").department(delivery).role(AgentRole.MANAGER).isAvailable(true).currentLoad(1).maxLoad(20).totalResolved(201).avgResolutionTimeHours(1.8).build(),
                Agent.builder().name("Meera Iyer").email("meera.iyer@complaintiq.com").phone("+91-9876543219").department(payment).role(AgentRole.MANAGER).isAvailable(true).currentLoad(0).maxLoad(20).totalResolved(178).avgResolutionTimeHours(2.1).build()
        ));
        agents.forEach(agent -> {
            UserRole userRole = switch (agent.getRole()) { case MANAGER -> UserRole.MANAGER; case TEAM_LEAD -> UserRole.TEAM_LEAD; default -> UserRole.AGENT; };
            AppUser agentUser = AppUser.builder().name(agent.getName()).email(agent.getEmail()).password(defaultPassword).role(userRole).agentId(agent.getId()).isActive(true).build();
            AppUser savedUser = appUserRepository.save(agentUser);
            agent.setUserId(savedUser.getId()); agentRepository.save(agent);
        });
        log.info("Agents seeded: {} agents | default password: Agent@123", agents.size());
        return agents;
    }

    private void updateDepartmentHeads(List<Department> departments, List<Agent> agents) {
        departments.get(0).setHeadAgentId(agents.get(0).getId()); departments.get(1).setHeadAgentId(agents.get(2).getId());
        departments.get(2).setHeadAgentId(agents.get(4).getId()); departments.get(3).setHeadAgentId(agents.get(5).getId());
        departments.get(4).setHeadAgentId(agents.get(7).getId());
        departmentRepository.saveAll(departments); log.info("Department heads assigned.");
    }

    private void seedAdminUsers() {
        if (!appUserRepository.existsByEmail("admin@complaintiq.com")) appUserRepository.save(AppUser.builder().name("System Admin").email("admin@complaintiq.com").password(passwordEncoder.encode("Admin@123")).role(UserRole.ADMIN).isActive(true).build());
        if (!appUserRepository.existsByEmail("manager1@complaintiq.com")) appUserRepository.save(AppUser.builder().name("Operations Manager").email("manager1@complaintiq.com").password(passwordEncoder.encode("Manager@123")).role(UserRole.MANAGER).isActive(true).build());
        if (!appUserRepository.existsByEmail("manager2@complaintiq.com")) appUserRepository.save(AppUser.builder().name("Quality Manager").email("manager2@complaintiq.com").password(passwordEncoder.encode("Manager@123")).role(UserRole.MANAGER).isActive(true).build());
        log.info("Admin/Manager users seeded — admin@complaintiq.com / Admin@123");
    }

    private List<Customer> seedCustomers() {
        if (customerRepository.count() > 0) return customerRepository.findAll();
        List<Customer> customers = customerRepository.saveAll(List.of(
                Customer.builder().name("Aarav Shah").email("aarav.shah@gmail.com").phone("+91-9001234567").tier(CustomerTier.NORMAL).totalComplaints(3).isActive(true).build(),
                Customer.builder().name("Ishaan Trivedi").email("ishaan.trivedi@gmail.com").phone("+91-9001234568").tier(CustomerTier.VIP).totalComplaints(7).isActive(true).build(),
                Customer.builder().name("Anika Gupta").email("anika.gupta@gmail.com").phone("+91-9001234569").tier(CustomerTier.PREMIUM).totalComplaints(12).isActive(true).build(),
                Customer.builder().name("Dhruv Malhotra").email("dhruv.malhotra@gmail.com").phone("+91-9001234570").tier(CustomerTier.NORMAL).totalComplaints(1).isActive(true).build(),
                Customer.builder().name("Sara Khan").email("sara.khan@gmail.com").phone("+91-9001234571").tier(CustomerTier.VIP).totalComplaints(5).isActive(true).build(),
                Customer.builder().name("Kabir Agarwal").email("kabir.agarwal@gmail.com").phone("+91-9001234572").tier(CustomerTier.NORMAL).totalComplaints(2).isActive(true).build(),
                Customer.builder().name("Diya Kapoor").email("diya.kapoor@gmail.com").phone("+91-9001234573").tier(CustomerTier.VIP).totalComplaints(4).isActive(true).build(),
                Customer.builder().name("Aryan Choudhary").email("aryan.choudhary@gmail.com").phone("+91-9001234574").tier(CustomerTier.PREMIUM).totalComplaints(9).isActive(true).build(),
                Customer.builder().name("Myra Bhatia").email("myra.bhatia@gmail.com").phone("+91-9001234575").tier(CustomerTier.NORMAL).totalComplaints(2).isActive(true).build(),
                Customer.builder().name("Vihaan Jain").email("vihaan.jain@gmail.com").phone("+91-9001234576").tier(CustomerTier.VIP).totalComplaints(6).isActive(true).build(),
                Customer.builder().name("Saanvi Desai").email("saanvi.desai@gmail.com").phone("+91-9001234577").tier(CustomerTier.NORMAL).totalComplaints(1).isActive(true).build(),
                Customer.builder().name("Ayaan Bose").email("ayaan.bose@gmail.com").phone("+91-9001234578").tier(CustomerTier.PREMIUM).totalComplaints(11).isActive(true).build(),
                Customer.builder().name("Kiara Menon").email("kiara.menon@gmail.com").phone("+91-9001234579").tier(CustomerTier.NORMAL).totalComplaints(3).isActive(true).build(),
                Customer.builder().name("Reyansh Pillai").email("reyansh.pillai@gmail.com").phone("+91-9001234580").tier(CustomerTier.VIP).totalComplaints(8).isActive(true).build(),
                Customer.builder().name("Anaya Rao").email("anaya.rao@gmail.com").phone("+91-9001234581").tier(CustomerTier.NORMAL).totalComplaints(2).isActive(true).build(),
                Customer.builder().name("Shaurya Saxena").email("shaurya.saxena@gmail.com").phone("+91-9001234582").tier(CustomerTier.NORMAL).totalComplaints(1).isActive(true).build(),
                Customer.builder().name("Navya Kulkarni").email("navya.kulkarni@gmail.com").phone("+91-9001234583").tier(CustomerTier.VIP).totalComplaints(5).isActive(true).build(),
                Customer.builder().name("Advait Mishra").email("advait.mishra@gmail.com").phone("+91-9001234584").tier(CustomerTier.PREMIUM).totalComplaints(10).isActive(true).build(),
                Customer.builder().name("Riya Chopra").email("riya.chopra@gmail.com").phone("+91-9001234585").tier(CustomerTier.NORMAL).totalComplaints(3).isActive(true).build(),
                Customer.builder().name("Ved Bhardwaj").email("ved.bhardwaj@gmail.com").phone("+91-9001234586").tier(CustomerTier.NORMAL).totalComplaints(2).isActive(true).build()
        ));
        customers.forEach(customer -> appUserRepository.save(AppUser.builder().name(customer.getName()).email(customer.getEmail()).password(passwordEncoder.encode("Customer@123")).role(UserRole.CUSTOMER).customerId(customer.getId()).isActive(true).build()));
        log.info("Customers seeded: {} customers | default password: Customer@123", customers.size());
        return customers;
    }

    private void seedComplaints(List<Customer> customers, List<Agent> agents, List<Department> departments) {
        if (complaintRepository.count() > 0) return;
        Department delivery = departments.get(0); Department payment = departments.get(1); Department product = departments.get(2);
        Department refund = departments.get(3); Department technical = departments.get(4);
        Agent deliveryLead = agents.get(0); Agent deliveryJr = agents.get(1);
        Agent paymentSenior = agents.get(2); Agent paymentJr = agents.get(3);
        Agent productSenior = agents.get(4);
        Agent refundLead = agents.get(5); Agent refundJr = agents.get(6);
        Agent techSenior = agents.get(7);
        Agent deliveryMgr = agents.get(8); Agent paymentMgr = agents.get(9);

        // ========== RECENT (LAST 24 HOURS) — OPEN & ASSIGNED ==========
        Complaint c1 = saveComplaint("Order not delivered after 3 weeks","I placed an order 3 weeks ago (Order #ORD-8821) and it has not been delivered yet. The tracking shows it has been stuck at the warehouse for 10 days. I need this resolved immediately or I will take legal action.","ORD-8821",customers.get(0),ComplaintChannel.WEB,ComplaintStatus.ASSIGNED,UrgencyLevel.CRITICAL,LocalDateTime.now().minusHours(1));
        saveAIAnalysis(c1,UrgencyLevel.CRITICAL,ComplaintCategory.DELIVERY,SentimentLevel.FURIOUS,ComplaintIntent.LEGAL_THREAT,"Customer threatens legal action over 3-week delayed delivery.","Contact logistics and escalate to courier partner.",0.95);
        saveAssignment(c1,deliveryLead,delivery,LocalDateTime.now().minusMinutes(50));
        logActivity(c1.getId(),"Complaint submitted",customers.get(0).getEmail()); logActivity(c1.getId(),"AI Analysis: CRITICAL/DELIVERY/FURIOUS","SYSTEM"); logActivity(c1.getId(),"Auto-assigned to " + deliveryLead.getName(),"SYSTEM");

        Complaint c2 = saveComplaint("Payment deducted but order cancelled","My payment of Rs. 2,450 was deducted but the order was auto-cancelled. No refund for 5 days.","ORD-4423",customers.get(1),ComplaintChannel.WEB,ComplaintStatus.IN_PROGRESS,UrgencyLevel.HIGH,LocalDateTime.now().minusHours(4));
        saveAIAnalysis(c2,UrgencyLevel.HIGH,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"VIP customer charged for cancelled order.","Initiate immediate refund.",0.91);
        saveAssignment(c2,paymentSenior,payment,LocalDateTime.now().minusHours(3));

        Complaint c3 = saveComplaint("Received wrong product","Ordered blue t-shirt (L), received red (M). Please exchange or refund.","ORD-7734",customers.get(2),ComplaintChannel.WEB,ComplaintStatus.ASSIGNED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusHours(6));
        saveAIAnalysis(c3,UrgencyLevel.MEDIUM,ComplaintCategory.PRODUCT,SentimentLevel.FRUSTRATED,ComplaintIntent.REPLACEMENT,"Customer received wrong product variant.","Arrange return pickup and dispatch correct product.",0.88);
        saveAssignment(c3,productSenior,product,LocalDateTime.now().minusHours(5));

        Complaint c4 = saveComplaint("App crashes when viewing order history","The mobile app crashes whenever I view order history. Android 13.",null,customers.get(3),ComplaintChannel.API,ComplaintStatus.OPEN,UrgencyLevel.LOW,LocalDateTime.now().minusHours(8));
        saveAIAnalysis(c4,UrgencyLevel.LOW,ComplaintCategory.TECHNICAL,SentimentLevel.NEUTRAL,ComplaintIntent.INFORMATION,"App crash on order history page.","File bug report.",0.82);

        Complaint c5 = saveComplaint("Discount coupon not applied","Coupon SAVE20 shows invalid at checkout. Not expired. Lost Rs. 500 discount.","ORD-5510",customers.get(4),ComplaintChannel.WEB,ComplaintStatus.ASSIGNED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusHours(12));
        saveAIAnalysis(c5,UrgencyLevel.MEDIUM,ComplaintCategory.PAYMENT,SentimentLevel.FRUSTRATED,ComplaintIntent.REFUND,"Valid coupon rejected at checkout.","Verify and apply credit.",0.85);
        saveAssignment(c5,paymentJr,payment,LocalDateTime.now().minusHours(11));

        Complaint c6 = saveComplaint("Received damaged laptop","Rs. 75,000 laptop arrived with cracked screen and broken keyboard.","ORD-9901",customers.get(5),ComplaintChannel.WEB,ComplaintStatus.IN_PROGRESS,UrgencyLevel.CRITICAL,LocalDateTime.now().minusHours(14));
        saveAIAnalysis(c6,UrgencyLevel.CRITICAL,ComplaintCategory.PRODUCT,SentimentLevel.FURIOUS,ComplaintIntent.REPLACEMENT,"Customer received heavily damaged expensive laptop.","Arrange same-day pickup and replacement.",0.97);
        saveAssignment(c6,productSenior,product,LocalDateTime.now().minusHours(13));

        Complaint c7 = saveComplaint("Unable to login after password reset","Password reset completed but login still fails with 'invalid credentials'.",null,customers.get(6),ComplaintChannel.WEB,ComplaintStatus.ASSIGNED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusHours(16));
        saveAIAnalysis(c7,UrgencyLevel.MEDIUM,ComplaintCategory.TECHNICAL,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Password reset flow broken.","Check reset token validity.",0.87);
        saveAssignment(c7,techSenior,technical,LocalDateTime.now().minusHours(15));

        Complaint c8 = saveComplaint("Subscription charged after cancellation","Cancelled premium plan on 1st, still charged Rs. 999 on 5th.",null,customers.get(7),ComplaintChannel.EMAIL,ComplaintStatus.OPEN,UrgencyLevel.HIGH,LocalDateTime.now().minusHours(18));
        saveAIAnalysis(c8,UrgencyLevel.HIGH,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"Charge after subscription cancel.","Verify cancellation date and refund.",0.90);

        Complaint c9 = saveComplaint("Wrong item size shipped","Ordered XL shoes, received L. Different from order confirmation.","ORD-1122",customers.get(8),ComplaintChannel.WEB,ComplaintStatus.ASSIGNED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusHours(20));
        saveAIAnalysis(c9,UrgencyLevel.MEDIUM,ComplaintCategory.PRODUCT,SentimentLevel.FRUSTRATED,ComplaintIntent.REPLACEMENT,"Size mismatch on shipped footwear.","Arrange exchange.",0.86);
        saveAssignment(c9,productSenior,product,LocalDateTime.now().minusHours(19));

        Complaint c10 = saveComplaint("Package stolen from doorstep","Delivery marked 'complete' but package never received. Doorbell footage shows nothing.","ORD-3344",customers.get(9),ComplaintChannel.WEB,ComplaintStatus.IN_PROGRESS,UrgencyLevel.HIGH,LocalDateTime.now().minusHours(22));
        saveAIAnalysis(c10,UrgencyLevel.HIGH,ComplaintCategory.DELIVERY,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"Delivery marked done but customer never received.","Investigate with courier and reship.",0.92);
        saveAssignment(c10,deliveryJr,delivery,LocalDateTime.now().minusHours(21));

        // ========== LAST 3 DAYS — MIX OF STATUSES ==========
        Complaint c11 = saveComplaint("Refund not processed after 2 weeks","Returned product 14 days ago, Rs. 8,999 refund still not credited.","ORD-2290",customers.get(10),ComplaintChannel.WEB,ComplaintStatus.ESCALATED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(2));
        saveAIAnalysis(c11,UrgencyLevel.HIGH,ComplaintCategory.REFUND,SentimentLevel.ANGRY,ComplaintIntent.ESCALATION,"VIP customer refund delayed 14 days.","Check with finance team.",0.93);
        saveAssignment(c11,refundLead,refund,LocalDateTime.now().minusDays(2).plusHours(1));

        Complaint c12 = saveComplaint("Quality issue with kitchenware","Non-stick pan coating peeled off after 3 uses. Product was Rs. 2,200.","ORD-7781",customers.get(11),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(2).minusHours(4));
        saveAIAnalysis(c12,UrgencyLevel.MEDIUM,ComplaintCategory.PRODUCT,SentimentLevel.FRUSTRATED,ComplaintIntent.REFUND,"Premium customer reports defective product.","Issue full refund.",0.89);
        saveAssignment(c12,productSenior,product,LocalDateTime.now().minusDays(2).minusHours(3));
        saveResolution(c12,productSenior,"Full refund of Rs. 2,200 processed. Customer informed via email.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(1).minusHours(6),5,"Very satisfied with quick resolution.");

        Complaint c13 = saveComplaint("Delivery agent was rude","The delivery person yelled at me when I asked for a receipt. Unprofessional.",null,customers.get(12),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(2).minusHours(8));
        saveAIAnalysis(c13,UrgencyLevel.MEDIUM,ComplaintCategory.DELIVERY,SentimentLevel.ANGRY,ComplaintIntent.ESCALATION,"Behavior complaint against delivery agent.","Investigate and take HR action.",0.87);
        saveAssignment(c13,deliveryLead,delivery,LocalDateTime.now().minusDays(2).minusHours(7));
        saveResolution(c13,deliveryLead,"Delivery agent counselled. HR warning issued. Customer received formal apology and Rs. 200 coupon.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(1).minusHours(20),4,"Appreciated the follow-up.");

        Complaint c14 = saveComplaint("EMI option not showing","Cannot select EMI at checkout for orders above Rs. 5,000 despite eligibility.",null,customers.get(13),ComplaintChannel.WEB,ComplaintStatus.ASSIGNED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(2).minusHours(12));
        saveAIAnalysis(c14,UrgencyLevel.LOW,ComplaintCategory.PAYMENT,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"EMI eligibility issue.","Check payment gateway config.",0.80);
        saveAssignment(c14,paymentJr,payment,LocalDateTime.now().minusDays(2).minusHours(11));

        Complaint c15 = saveComplaint("Website checkout error 500","Cannot complete purchase - getting server error on final step.",null,customers.get(14),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(2).minusHours(18));
        saveAIAnalysis(c15,UrgencyLevel.HIGH,ComplaintCategory.TECHNICAL,SentimentLevel.ANGRY,ComplaintIntent.INFORMATION,"Checkout failing with 500 error.","Debug and hotfix.",0.91);
        saveAssignment(c15,techSenior,technical,LocalDateTime.now().minusDays(2).minusHours(17));
        saveResolution(c15,techSenior,"Bug in coupon validation service identified and fixed. Deployed hotfix v2.4.1.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(2).minusHours(14),5,"Fast technical fix, thanks!");

        Complaint c16 = saveComplaint("Partial refund received","I was supposed to get Rs. 3,500 refund but only received Rs. 2,800. Missing Rs. 700.","ORD-5567",customers.get(15),ComplaintChannel.EMAIL,ComplaintStatus.IN_PROGRESS,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(3));
        saveAIAnalysis(c16,UrgencyLevel.MEDIUM,ComplaintCategory.REFUND,SentimentLevel.FRUSTRATED,ComplaintIntent.REFUND,"Partial refund issue - Rs. 700 shortfall.","Investigate and credit balance.",0.88);
        saveAssignment(c16,refundJr,refund,LocalDateTime.now().minusDays(3).plusHours(1));

        Complaint c17 = saveComplaint("Missing items in order","Ordered 4 items, received only 3. Shampoo bottle missing from package.","ORD-9988",customers.get(16),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(3).minusHours(6));
        saveAIAnalysis(c17,UrgencyLevel.MEDIUM,ComplaintCategory.DELIVERY,SentimentLevel.FRUSTRATED,ComplaintIntent.REPLACEMENT,"Missing item in multi-item shipment.","Dispatch missing item.",0.89);
        saveAssignment(c17,deliveryJr,delivery,LocalDateTime.now().minusDays(3).minusHours(5));
        saveResolution(c17,deliveryJr,"Missing shampoo dispatched via express same day. Tracking shared.",ResolutionType.REPLACEMENT_SENT,LocalDateTime.now().minusDays(2).minusHours(22),5,"Perfect resolution!");

        Complaint c18 = saveComplaint("Fake product received","The Samsung earphones I received are clearly fake - packaging and serial number differ from Samsung website.","ORD-4490",customers.get(17),ComplaintChannel.WEB,ComplaintStatus.ESCALATED,UrgencyLevel.CRITICAL,LocalDateTime.now().minusDays(3).minusHours(10));
        saveAIAnalysis(c18,UrgencyLevel.CRITICAL,ComplaintCategory.PRODUCT,SentimentLevel.FURIOUS,ComplaintIntent.LEGAL_THREAT,"Counterfeit product allegation from PREMIUM customer.","Immediate quality team involvement; legal review if confirmed.",0.96);
        saveAssignment(c18,productSenior,product,LocalDateTime.now().minusDays(3).minusHours(9));

        Complaint c19 = saveComplaint("Product description mismatch","Listed as 128GB storage, product is 64GB. Misleading.","ORD-3341",customers.get(18),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(3).minusHours(14));
        saveAIAnalysis(c19,UrgencyLevel.LOW,ComplaintCategory.PRODUCT,SentimentLevel.CALM,ComplaintIntent.INFORMATION,"Incorrect product spec on listing.","Update catalogue.",0.80);
        saveAssignment(c19,productSenior,product,LocalDateTime.now().minusDays(3).minusHours(13));
        saveResolution(c19,productSenior,"Listing updated. Customer offered 10% discount as goodwill.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(2).minusHours(20),3,null);

        Complaint c20 = saveComplaint("COD rejected at door","Delivery agent refused to accept cash even though COD was confirmed.","ORD-6612",customers.get(19),ComplaintChannel.WHATSAPP,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(4));
        saveAIAnalysis(c20,UrgencyLevel.MEDIUM,ComplaintCategory.DELIVERY,SentimentLevel.FRUSTRATED,ComplaintIntent.ESCALATION,"COD order not accepted at delivery.","Reschedule delivery and counsel agent.",0.85);
        saveAssignment(c20,deliveryJr,delivery,LocalDateTime.now().minusDays(4).plusHours(1));
        saveResolution(c20,deliveryJr,"Delivery re-attempted and completed. Agent briefed on COD policy.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(3).minusHours(12),4,"Got my order, thanks.");

        // ========== 5-10 DAYS AGO — MOSTLY RESOLVED ==========
        Complaint c21 = saveComplaint("Card declined but bank shows debit","Card declined at checkout but Rs. 1,250 debited. Order never placed.","ORD-PENDING",customers.get(0),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(5));
        saveAIAnalysis(c21,UrgencyLevel.HIGH,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"Failed transaction with debit.","Reverse transaction via gateway.",0.92);
        saveAssignment(c21,paymentSenior,payment,LocalDateTime.now().minusDays(5).plusHours(1));
        saveResolution(c21,paymentSenior,"Gateway mismatch identified. Refund of Rs. 1,250 credited within 24 hours.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(4),5,"Very professional!");

        Complaint c22 = saveComplaint("Order stuck in transit","Order shipped 7 days ago, tracking shows 'in transit' since day 2.","ORD-7820",customers.get(1),ComplaintChannel.EMAIL,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(5).minusHours(4));
        saveAIAnalysis(c22,UrgencyLevel.MEDIUM,ComplaintCategory.DELIVERY,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Stuck shipment needs tracing.","Trace with courier.",0.86);
        saveAssignment(c22,deliveryLead,delivery,LocalDateTime.now().minusDays(5).minusHours(3));
        saveResolution(c22,deliveryLead,"Package located at intermediate hub. Expedited to customer, delivered next day.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(3),5,"Excellent follow-up.");

        Complaint c23 = saveComplaint("Wrong tax charged","Invoice shows 28% GST on apparel item that should be 5%.","ORD-4421",customers.get(2),ComplaintChannel.EMAIL,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(6));
        saveAIAnalysis(c23,UrgencyLevel.MEDIUM,ComplaintCategory.PAYMENT,SentimentLevel.FRUSTRATED,ComplaintIntent.REFUND,"Incorrect tax rate applied.","Correct invoice and refund difference.",0.88);
        saveAssignment(c23,paymentSenior,payment,LocalDateTime.now().minusDays(6).plusHours(1));
        saveResolution(c23,paymentSenior,"Corrected invoice issued. Rs. 560 tax difference refunded.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(5),5,"Thank you for quick fix.");

        Complaint c24 = saveComplaint("Wallet balance not credited","Added Rs. 1,000 to wallet but it shows Rs. 0. Bank has debited the amount.",null,customers.get(3),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(7));
        saveAIAnalysis(c24,UrgencyLevel.HIGH,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"Wallet top-up failed after debit.","Reconcile wallet service.",0.93);
        saveAssignment(c24,paymentMgr,payment,LocalDateTime.now().minusDays(7).plusHours(1));
        saveResolution(c24,paymentMgr,"Wallet sync issue fixed. Rs. 1,000 credited.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(6).minusHours(18),5,"Grateful for speedy action.");

        Complaint c25 = saveComplaint("Return pickup not happened","Return was scheduled 5 days ago, no one showed up despite 3 calls.","ORD-5599",customers.get(4),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(7).minusHours(6));
        saveAIAnalysis(c25,UrgencyLevel.MEDIUM,ComplaintCategory.REFUND,SentimentLevel.FRUSTRATED,ComplaintIntent.ESCALATION,"Return pickup failure - 5 day delay.","Force-schedule pickup.",0.87);
        saveAssignment(c25,refundLead,refund,LocalDateTime.now().minusDays(7).minusHours(5));
        saveResolution(c25,refundLead,"Pickup completed. Refund initiated post-inspection.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(6),4,"Finally resolved.");

        Complaint c26 = saveComplaint("Cashback not received","Offer promised 10% cashback on Rs. 5,000 order. 7 days passed, no credit.","ORD-8812",customers.get(5),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(8));
        saveAIAnalysis(c26,UrgencyLevel.LOW,ComplaintCategory.PAYMENT,SentimentLevel.FRUSTRATED,ComplaintIntent.REFUND,"Promotional cashback not honored.","Apply cashback manually.",0.83);
        saveAssignment(c26,paymentJr,payment,LocalDateTime.now().minusDays(8).plusHours(1));
        saveResolution(c26,paymentJr,"Rs. 500 cashback manually credited to wallet.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(7).minusHours(14),5,"Appreciate it.");

        Complaint c27 = saveComplaint("Defective TV","65-inch LED TV flickers after 2 days. Clearly defective.","ORD-2233",customers.get(6),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(9));
        saveAIAnalysis(c27,UrgencyLevel.HIGH,ComplaintCategory.PRODUCT,SentimentLevel.ANGRY,ComplaintIntent.REPLACEMENT,"New TV defective within 48 hours.","Replace unit.",0.94);
        saveAssignment(c27,productSenior,product,LocalDateTime.now().minusDays(9).plusHours(1));
        saveResolution(c27,productSenior,"Replacement TV dispatched. Old unit pickup completed.",ResolutionType.REPLACEMENT_SENT,LocalDateTime.now().minusDays(7),5,"Great service!");

        Complaint c28 = saveComplaint("Stuck at 'Preparing to ship'","Order placed 4 days ago, still shows 'Preparing to ship'.","ORD-6677",customers.get(7),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(9).minusHours(8));
        saveAIAnalysis(c28,UrgencyLevel.MEDIUM,ComplaintCategory.DELIVERY,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Order stuck in warehouse.","Expedite dispatch.",0.85);
        saveAssignment(c28,deliveryJr,delivery,LocalDateTime.now().minusDays(9).minusHours(7));
        saveResolution(c28,deliveryJr,"Warehouse inventory issue resolved. Order shipped same day.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(8),4,"Thanks.");

        Complaint c29 = saveComplaint("Gift wrap request ignored","Paid Rs. 99 for gift wrapping, product arrived without it.","ORD-9900",customers.get(8),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(10));
        saveAIAnalysis(c29,UrgencyLevel.LOW,ComplaintCategory.PRODUCT,SentimentLevel.FRUSTRATED,ComplaintIntent.REFUND,"Paid add-on service not delivered.","Refund gift wrap charge.",0.82);
        saveAssignment(c29,productSenior,product,LocalDateTime.now().minusDays(10).plusHours(2));
        saveResolution(c29,productSenior,"Gift wrap fee of Rs. 99 refunded. Warehouse re-trained.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(9),3,null);

        Complaint c30 = saveComplaint("Broken packaging, product fine","Box arrived crushed but contents undamaged. Just reporting for warehouse feedback.","ORD-1188",customers.get(9),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(10).minusHours(10));
        saveAIAnalysis(c30,UrgencyLevel.LOW,ComplaintCategory.DELIVERY,SentimentLevel.CALM,ComplaintIntent.INFORMATION,"Cosmetic packaging issue.","Log for process improvement.",0.78);
        saveAssignment(c30,deliveryJr,delivery,LocalDateTime.now().minusDays(10).minusHours(9));
        saveResolution(c30,deliveryJr,"Feedback forwarded to packaging team. Customer thanked.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(9).minusHours(18),4,"Minor issue, thanks for listening.");

        // ========== 11-20 DAYS AGO — HISTORICAL RESOLVED ==========
        Complaint c31 = saveComplaint("Invoice not received on email","No invoice email sent post-purchase. Need for reimbursement.","ORD-3300",customers.get(10),ComplaintChannel.EMAIL,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(11));
        saveAIAnalysis(c31,UrgencyLevel.LOW,ComplaintCategory.TECHNICAL,SentimentLevel.NEUTRAL,ComplaintIntent.INFORMATION,"Missing invoice email.","Resend invoice.",0.84);
        saveAssignment(c31,techSenior,technical,LocalDateTime.now().minusDays(11).plusHours(1));
        saveResolution(c31,techSenior,"Invoice emailed manually. Email service bug fix tracked.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(10),5,"Sorted in minutes!");

        Complaint c32 = saveComplaint("Extra item received","Received an extra bottle I didn't order. What to do?","ORD-7722",customers.get(11),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(12));
        saveAIAnalysis(c32,UrgencyLevel.LOW,ComplaintCategory.DELIVERY,SentimentLevel.CALM,ComplaintIntent.INFORMATION,"Customer reports extra item.","Thank customer, mark as goodwill.",0.79);
        saveAssignment(c32,deliveryJr,delivery,LocalDateTime.now().minusDays(12).plusHours(2));
        saveResolution(c32,deliveryJr,"Customer told to keep as goodwill. Warehouse SKU count reconciled.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(11),5,"Very nice of you!");

        Complaint c33 = saveComplaint("Incorrect color variant","Ordered sky blue, received navy blue. Color options misleading.","ORD-4455",customers.get(12),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(13));
        saveAIAnalysis(c33,UrgencyLevel.MEDIUM,ComplaintCategory.PRODUCT,SentimentLevel.FRUSTRATED,ComplaintIntent.REPLACEMENT,"Color variant mismatch.","Exchange.",0.86);
        saveAssignment(c33,productSenior,product,LocalDateTime.now().minusDays(13).plusHours(1));
        saveResolution(c33,productSenior,"Correct color dispatched. Old item picked up same trip.",ResolutionType.REPLACEMENT_SENT,LocalDateTime.now().minusDays(12),4,"Thanks.");

        Complaint c34 = saveComplaint("Service area not covered","Can't deliver to my pincode 122018. No alternative offered.",null,customers.get(13),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(14));
        saveAIAnalysis(c34,UrgencyLevel.LOW,ComplaintCategory.DELIVERY,SentimentLevel.CALM,ComplaintIntent.INFORMATION,"Service-area inquiry.","Inform about expansion plan.",0.80);
        saveAssignment(c34,deliveryLead,delivery,LocalDateTime.now().minusDays(14).plusHours(2));
        saveResolution(c34,deliveryLead,"Customer informed of Q2 expansion plan. Added to notify list.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(13),4,"Appreciate the transparency.");

        Complaint c35 = saveComplaint("OTP not received","Cannot complete checkout - OTP never arrives on registered number.",null,customers.get(14),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(15));
        saveAIAnalysis(c35,UrgencyLevel.MEDIUM,ComplaintCategory.TECHNICAL,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"SMS-OTP delivery issue.","Check SMS gateway.",0.87);
        saveAssignment(c35,techSenior,technical,LocalDateTime.now().minusDays(15).plusHours(1));
        saveResolution(c35,techSenior,"DLT template issue resolved with SMS provider. Customer confirmed receipt.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(14),5,"Smooth.");

        Complaint c36 = saveComplaint("Late night delivery disturbance","Delivery attempted at 11pm, disturbed whole family.","ORD-6655",customers.get(15),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(16));
        saveAIAnalysis(c36,UrgencyLevel.LOW,ComplaintCategory.DELIVERY,SentimentLevel.FRUSTRATED,ComplaintIntent.ESCALATION,"Off-hour delivery attempt.","Flag in courier SLA.",0.81);
        saveAssignment(c36,deliveryLead,delivery,LocalDateTime.now().minusDays(16).plusHours(2));
        saveResolution(c36,deliveryLead,"Courier partner reminded of 7am-9pm window. Customer received formal apology.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(15),3,null);

        Complaint c37 = saveComplaint("Missed promotion window","Site showed 'sale ending in 10 mins' but ended 5 mins after I saw it.",null,customers.get(16),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(17));
        saveAIAnalysis(c37,UrgencyLevel.LOW,ComplaintCategory.TECHNICAL,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Timing mismatch on promo countdown.","Fix countdown sync.",0.78);
        saveAssignment(c37,techSenior,technical,LocalDateTime.now().minusDays(17).plusHours(3));
        saveResolution(c37,techSenior,"Countdown timer timezone bug fixed. Customer offered manual 10% discount.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(16),3,null);

        Complaint c38 = saveComplaint("Duplicate order charged","Clicked checkout once but two identical orders placed and both charged.","ORD-8899",customers.get(17),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(18));
        saveAIAnalysis(c38,UrgencyLevel.HIGH,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"Duplicate order submission.","Cancel duplicate and refund.",0.92);
        saveAssignment(c38,paymentSenior,payment,LocalDateTime.now().minusDays(18).plusHours(1));
        saveResolution(c38,paymentSenior,"Duplicate order cancelled, Rs. 2,199 refunded within 24 hours.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(17),5,"Fast fix.");

        Complaint c39 = saveComplaint("Warranty card missing","TV came without warranty card. How to claim warranty?","ORD-5522",customers.get(18),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(19));
        saveAIAnalysis(c39,UrgencyLevel.MEDIUM,ComplaintCategory.PRODUCT,SentimentLevel.NEUTRAL,ComplaintIntent.INFORMATION,"Missing warranty paperwork.","Send digital warranty.",0.85);
        saveAssignment(c39,productSenior,product,LocalDateTime.now().minusDays(19).plusHours(2));
        saveResolution(c39,productSenior,"Digital warranty + hard copy mailed. Serial number registered with manufacturer.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(18),4,"Clear communication.");

        Complaint c40 = saveComplaint("Product expired on arrival","Skincare product expiry date is next month. Only 3 weeks usable.","ORD-4477",customers.get(19),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(20));
        saveAIAnalysis(c40,UrgencyLevel.HIGH,ComplaintCategory.PRODUCT,SentimentLevel.ANGRY,ComplaintIntent.REPLACEMENT,"Near-expiry product shipped.","Replace with fresh stock.",0.93);
        saveAssignment(c40,productSenior,product,LocalDateTime.now().minusDays(20).plusHours(1));
        saveResolution(c40,productSenior,"Fresh stock dispatched with 12-month expiry. Original product return collected.",ResolutionType.REPLACEMENT_SENT,LocalDateTime.now().minusDays(19),5,"Perfect.");

        // ========== 21-30 DAYS AGO — ALL CLOSED ==========
        Complaint c41 = saveComplaint("Double wallet debit","Rs. 800 debited twice from wallet for same order.",null,customers.get(0),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(21));
        saveAIAnalysis(c41,UrgencyLevel.HIGH,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"Wallet double-charge.","Reverse duplicate.",0.91);
        saveAssignment(c41,paymentSenior,payment,LocalDateTime.now().minusDays(21).plusHours(1));
        saveResolution(c41,paymentSenior,"Duplicate Rs. 800 charge reversed to wallet.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(20),5,"Thanks.");

        Complaint c42 = saveComplaint("Incorrect address auto-filled","Checkout picked wrong saved address even after I selected another.","ORD-9911",customers.get(1),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(22));
        saveAIAnalysis(c42,UrgencyLevel.MEDIUM,ComplaintCategory.TECHNICAL,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Address selection bug.","Fix UI binding.",0.86);
        saveAssignment(c42,techSenior,technical,LocalDateTime.now().minusDays(22).plusHours(2));
        saveResolution(c42,techSenior,"Address selector bug fixed. Order re-routed to correct address.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(21),4,"Resolved.");

        Complaint c43 = saveComplaint("Price change post-order","Product price dropped Rs. 500 right after I ordered. Want price-match.","ORD-2288",customers.get(2),ComplaintChannel.EMAIL,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(23));
        saveAIAnalysis(c43,UrgencyLevel.LOW,ComplaintCategory.PAYMENT,SentimentLevel.FRUSTRATED,ComplaintIntent.REFUND,"Post-purchase price-drop.","Offer partial refund.",0.82);
        saveAssignment(c43,paymentJr,payment,LocalDateTime.now().minusDays(23).plusHours(3));
        saveResolution(c43,paymentJr,"Rs. 500 price-match credit issued as store credit.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(22),4,"Fair resolution.");

        Complaint c44 = saveComplaint("Poor product fit","Jeans labeled 32 fit like 34. Sizing inconsistent.","ORD-3355",customers.get(3),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(24));
        saveAIAnalysis(c44,UrgencyLevel.LOW,ComplaintCategory.PRODUCT,SentimentLevel.FRUSTRATED,ComplaintIntent.REPLACEMENT,"Apparel sizing inconsistent.","Offer exchange.",0.80);
        saveAssignment(c44,productSenior,product,LocalDateTime.now().minusDays(24).plusHours(2));
        saveResolution(c44,productSenior,"Exchange with size 30 processed. Return shipping free.",ResolutionType.REPLACEMENT_SENT,LocalDateTime.now().minusDays(23),3,null);

        Complaint c45 = saveComplaint("Scheduled delivery missed","Chose delivery slot 10am-12pm, package came at 4pm.","ORD-6688",customers.get(4),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(25));
        saveAIAnalysis(c45,UrgencyLevel.LOW,ComplaintCategory.DELIVERY,SentimentLevel.FRUSTRATED,ComplaintIntent.ESCALATION,"SLA window miss.","Log for courier SLA review.",0.81);
        saveAssignment(c45,deliveryJr,delivery,LocalDateTime.now().minusDays(25).plusHours(3));
        saveResolution(c45,deliveryJr,"Courier partner penalised. Customer offered free delivery on next order.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(24),3,null);

        Complaint c46 = saveComplaint("Notification spam","Getting 10+ promotional push notifications per day. Unsubscribe broken.",null,customers.get(5),ComplaintChannel.API,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(26));
        saveAIAnalysis(c46,UrgencyLevel.LOW,ComplaintCategory.TECHNICAL,SentimentLevel.ANGRY,ComplaintIntent.INFORMATION,"Unsubscribe not working.","Fix preference flow.",0.84);
        saveAssignment(c46,techSenior,technical,LocalDateTime.now().minusDays(26).plusHours(2));
        saveResolution(c46,techSenior,"Preference manager bug fixed. All promo notifications disabled for account.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(25),4,"Peace restored!");

        Complaint c47 = saveComplaint("UPI payment stuck","UPI debited but order not confirmed on site. 30 mins waiting.","ORD-7799",customers.get(6),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(27));
        saveAIAnalysis(c47,UrgencyLevel.HIGH,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"UPI webhook timeout.","Reconcile or refund.",0.90);
        saveAssignment(c47,paymentSenior,payment,LocalDateTime.now().minusDays(27).plusHours(1));
        saveResolution(c47,paymentSenior,"UPI transaction reconciled, order confirmed without re-charge.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(26),5,"Great recovery.");

        Complaint c48 = saveComplaint("Product listing removed mid-order","Item was in cart, tried to checkout, listing had been delisted.",null,customers.get(7),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(28));
        saveAIAnalysis(c48,UrgencyLevel.LOW,ComplaintCategory.TECHNICAL,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Cart-listing race condition.","Improve cart sync.",0.79);
        saveAssignment(c48,techSenior,technical,LocalDateTime.now().minusDays(28).plusHours(4));
        saveResolution(c48,techSenior,"Cart cleanup workflow improved. Customer notified of similar alternatives.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(27),3,null);

        Complaint c49 = saveComplaint("Loyalty points expired without notice","15,000 points silently expired last week. No expiry reminder.",null,customers.get(8),ComplaintChannel.EMAIL,ComplaintStatus.CLOSED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(29));
        saveAIAnalysis(c49,UrgencyLevel.MEDIUM,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"Loyalty points expired silently.","Reinstate as goodwill.",0.87);
        saveAssignment(c49,paymentMgr,payment,LocalDateTime.now().minusDays(29).plusHours(2));
        saveResolution(c49,paymentMgr,"50% of points (7,500) reinstated as goodwill. Email reminder system enabled for future.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(28),4,"Fair compromise.");

        Complaint c50 = saveComplaint("Cancelled order still shipped","Cancelled order 3 hours after placing, but it still got shipped.","ORD-1144",customers.get(9),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(30));
        saveAIAnalysis(c50,UrgencyLevel.MEDIUM,ComplaintCategory.DELIVERY,SentimentLevel.FRUSTRATED,ComplaintIntent.REFUND,"Cancel race-condition: order shipped after cancel.","Accept return and refund.",0.88);
        saveAssignment(c50,deliveryMgr,delivery,LocalDateTime.now().minusDays(30).plusHours(1));
        saveResolution(c50,deliveryMgr,"Return arranged and full refund processed. Cancellation pipeline fix tracked.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(29),4,"Thanks for owning it.");

        // ========== EXTRA VARIETY — LAST 5 COMPLAINTS ==========
        Complaint c51 = saveComplaint("Can't redeem gift card","Rs. 2,000 gift card won't apply at checkout.",null,customers.get(10),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.MEDIUM,LocalDateTime.now().minusDays(18).minusHours(6));
        saveAIAnalysis(c51,UrgencyLevel.MEDIUM,ComplaintCategory.PAYMENT,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Gift card redemption failing.","Verify card status.",0.85);
        saveAssignment(c51,paymentSenior,payment,LocalDateTime.now().minusDays(18).minusHours(5));
        saveResolution(c51,paymentSenior,"Gift card had been frozen due to security check. Unfrozen and tested successfully.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(17).minusHours(18),4,"Glad it worked.");

        Complaint c52 = saveComplaint("Wrong billing currency","Shown in USD instead of INR on checkout.",null,customers.get(11),ComplaintChannel.WEB,ComplaintStatus.CLOSED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(15).minusHours(10));
        saveAIAnalysis(c52,UrgencyLevel.LOW,ComplaintCategory.TECHNICAL,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Currency localization issue.","Fix locale detection.",0.82);
        saveAssignment(c52,techSenior,technical,LocalDateTime.now().minusDays(15).minusHours(9));
        saveResolution(c52,techSenior,"Locale detection bug fixed in checkout service.",ResolutionType.INFORMATION_PROVIDED,LocalDateTime.now().minusDays(14),3,null);

        Complaint c53 = saveComplaint("Fragile item broke in transit","Glass decor piece shattered on arrival.","ORD-5544",customers.get(12),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(6).minusHours(3));
        saveAIAnalysis(c53,UrgencyLevel.HIGH,ComplaintCategory.DELIVERY,SentimentLevel.ANGRY,ComplaintIntent.REPLACEMENT,"Fragile item damage in transit.","Replace with bubble-wrap.",0.90);
        saveAssignment(c53,deliveryLead,delivery,LocalDateTime.now().minusDays(6).minusHours(2));
        saveResolution(c53,deliveryLead,"Replacement dispatched with premium packaging. Courier briefed.",ResolutionType.REPLACEMENT_SENT,LocalDateTime.now().minusDays(5),5,"Thanks for careful redelivery.");

        Complaint c54 = saveComplaint("Wrong credit card charged","My saved card A was selected but card B got charged.","ORD-7766",customers.get(13),ComplaintChannel.WEB,ComplaintStatus.RESOLVED,UrgencyLevel.HIGH,LocalDateTime.now().minusDays(4).minusHours(5));
        saveAIAnalysis(c54,UrgencyLevel.HIGH,ComplaintCategory.PAYMENT,SentimentLevel.ANGRY,ComplaintIntent.REFUND,"Wrong payment source used.","Refund and investigate.",0.89);
        saveAssignment(c54,paymentSenior,payment,LocalDateTime.now().minusDays(4).minusHours(4));
        saveResolution(c54,paymentSenior,"Card B refunded, card A charged correctly. UI bug in card selector fixed.",ResolutionType.REFUND_ISSUED,LocalDateTime.now().minusDays(3).minusHours(12),5,"Impressed with follow-up.");

        Complaint c55 = saveComplaint("Cannot download invoice PDF","Invoice PDF button does nothing on web. Tried 3 browsers.",null,customers.get(14),ComplaintChannel.WEB,ComplaintStatus.ASSIGNED,UrgencyLevel.LOW,LocalDateTime.now().minusDays(1).minusHours(6));
        saveAIAnalysis(c55,UrgencyLevel.LOW,ComplaintCategory.TECHNICAL,SentimentLevel.FRUSTRATED,ComplaintIntent.INFORMATION,"Invoice download broken.","Fix PDF service.",0.83);
        saveAssignment(c55,techSenior,technical,LocalDateTime.now().minusDays(1).minusHours(5));

        setSLADeadlines();
        complaintRepository.findMaxTicketSequence().ifPresent(ticketIdGenerator::initSequence);
        log.info("Complaints seeded: 55 sample complaints across 30 days");
    }

    private void setSLADeadlines() {
        complaintRepository.findAll().forEach(complaint -> {
            if (complaint.getUrgency() != null && complaint.getSlaDeadline() == null && complaint.getCreatedAt() != null) {
                slaConfigRepository.findByUrgencyLevel(complaint.getUrgency()).ifPresent(config -> {
                    complaint.setSlaDeadline(complaint.getCreatedAt().plusHours(config.getResolutionHours()));
                    complaintRepository.save(complaint);
                });
            }
        });
    }

    private Complaint saveComplaint(String title, String description, String orderId, Customer customer, ComplaintChannel channel, ComplaintStatus status, UrgencyLevel urgency, LocalDateTime createdAt) {
        String ticketId = ticketIdGenerator.generate();
        return complaintRepository.save(Complaint.builder().ticketId(ticketId).customer(customer).title(title).description(description).orderId(orderId).channel(channel).status(status).urgency(urgency).build());
    }
    private void saveAIAnalysis(Complaint complaint, UrgencyLevel urgency, ComplaintCategory category, SentimentLevel sentiment, ComplaintIntent intent, String summary, String suggestedAction, Double confidence) {
        aiAnalysisRepository.save(AIAnalysis.builder().complaint(complaint).urgency(urgency).category(category).sentiment(sentiment).intent(intent).summary(summary).suggestedAction(suggestedAction).confidenceScore(confidence).rawResponse("SEEDED").analyzedAt(LocalDateTime.now()).build());
    }
    private void saveAssignment(Complaint complaint, Agent agent, Department department, LocalDateTime assignedAt) {
        assignmentRepository.save(Assignment.builder().complaint(complaint).agent(agent).department(department).status(AssignmentStatus.ASSIGNED).assignedAt(assignedAt).build());
    }
    private void saveResolution(Complaint complaint, Agent agent, String note, ResolutionType type, LocalDateTime resolvedAt, Integer score, String feedback) {
        complaint.setResolvedAt(resolvedAt); complaintRepository.save(complaint);
        resolutionRepository.save(Resolution.builder().complaint(complaint).resolvedBy(agent).resolutionNote(note).resolutionType(type).resolvedAt(resolvedAt).satisfactionScore(score).feedbackComment(feedback).build());
    }
    private void logActivity(Long complaintId, String action, String performedBy) {
        activityRepository.save(ComplaintActivity.builder().complaintId(complaintId).action(action).performedBy(performedBy).performedAt(LocalDateTime.now()).build());
    }
}