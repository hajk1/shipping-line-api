package com.shipping.freightops.service;

import com.shipping.freightops.dto.CreateCustomerRequest;
import com.shipping.freightops.entity.Customer;
import com.shipping.freightops.repository.CustomerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Transactional
  public Customer createCustomer(CreateCustomerRequest request) {
    Customer customer = new Customer();
    customer.setCompanyName(request.getCompanyName());
    customer.setContactName(request.getContactName());
    customer.setEmail(request.getEmail());
    customer.setPhone(request.getPhone());
    customer.setAddress(request.getAddress());
    return customerRepository.save(customer);
  }

  @Transactional(readOnly = true)
  public Customer getCustomer(Long id) {
    return customerRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
  }

  @Transactional(readOnly = true)
  public List<Customer> getAllCustomers() {
    return customerRepository.findAll();
  }
}
