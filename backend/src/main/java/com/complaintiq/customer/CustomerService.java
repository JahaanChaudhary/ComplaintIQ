package com.complaintiq.customer;
import com.complaintiq.common.PagedResponse;
import com.complaintiq.customer.enums.CustomerTier;
import com.complaintiq.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Slf4j @Service @RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    @Transactional(readOnly=true) public PagedResponse<Customer> getAllCustomers(int page, int size) {
        Page<Customer> customers = customerRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.from(customers);
    }
    @Transactional(readOnly=true) public Customer getById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer","id",id));
    }
    @Transactional(readOnly=true) public Customer getByEmail(String email) {
        return customerRepository.findActiveByEmail(email.toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("Customer","email",email));
    }
    @Transactional public Customer updateTier(Long customerId, CustomerTier tier) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer","id",customerId));
        customer.setTier(tier); Customer saved = customerRepository.save(customer);
        log.info("Customer tier updated: customerId={} tier={}", customerId, tier); return saved;
    }
    @Transactional public void deactivateCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer","id",customerId));
        customer.setIsActive(false); customerRepository.save(customer);
        log.info("Customer deactivated: customerId={}", customerId);
    }
    @Transactional(readOnly=true) public long countByTier(CustomerTier tier) { return customerRepository.countByTier(tier); }
}
