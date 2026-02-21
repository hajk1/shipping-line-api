package com.shipping.freightops.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.shipping.freightops.dto.CreateFreightOrderRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.exception.BadRequestException;
import com.shipping.freightops.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class FreightOrderServiceTest {
  @Autowired private FreightOrderService freightOrderService;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private PortRepository portRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private VoyagePriceRepository voyagePriceRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;

  private Voyage savedVoyage;
  private Container savedContainer;
  private Customer savedCustomer;

  @BeforeEach
  void setUp() {
    freightOrderRepository.deleteAll();
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();

    Port departure = portRepository.save(new Port("AEJEA", "Jebel Ali", "UAE"));
    Port arrival = portRepository.save(new Port("CNSHA", "Shanghai", "China"));

    Vessel vessel = vesselRepository.save(new Vessel("MV Test", "9999999", 3000));

    Voyage voyage = new Voyage();
    voyage.setVoyageNumber("VOY-001");
    voyage.setVessel(vessel);
    voyage.setDeparturePort(departure);
    voyage.setArrivalPort(arrival);
    voyage.setDepartureTime(LocalDateTime.now().plusDays(3));
    voyage.setArrivalTime(LocalDateTime.now().plusDays(10));
    savedVoyage = voyageRepository.save(voyage);

    savedContainer =
        containerRepository.save(
            new Container("TSTU1234567", ContainerSize.TWENTY_FOOT, ContainerType.DRY));

    Customer customer = new Customer();
    customer.setCompanyName("Test Customer Inc.");
    customer.setContactName("John Doe");
    customer.setEmail("john@test.com");
    savedCustomer = customerRepository.save(customer);

    VoyagePrice price = new VoyagePrice();
    price.setVoyage(savedVoyage);
    price.setContainerSize(ContainerSize.TWENTY_FOOT);
    price.setBasePriceUsd(BigDecimal.valueOf(1000));
    voyagePriceRepository.save(price);
  }

  @Test
  @DisplayName("createOrder → calculates final price with discount")
  void createOrder_withDiscount_appliesCorrectPrice() {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("tester");
    request.setDiscountPercent(BigDecimal.valueOf(10)); // 10%

    FreightOrder order = freightOrderService.createOrder(request);

    assertThat(order.getBasePriceUsd()).isEqualByComparingTo("1000");
    assertThat(order.getDiscountPercent()).isEqualByComparingTo("10");
    assertThat(order.getFinalPrice()).isEqualByComparingTo("900");
  }

  @Test
  @DisplayName("createOrder → no discount means full price")
  void createOrder_withoutDiscount_setsFullPrice() {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("tester");

    FreightOrder order = freightOrderService.createOrder(request);

    assertThat(order.getDiscountPercent()).isEqualByComparingTo("0");
    assertThat(order.getFinalPrice()).isEqualByComparingTo("1000");
  }

  @Test
  @DisplayName("createOrder → throws when no price defined")
  void createOrder_withoutVoyagePrice_throwsException() {
    voyagePriceRepository.deleteAll(); // remove price

    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());

    assertThatThrownBy(() -> freightOrderService.createOrder(request))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("createOrder → throws when voyage is cancelled")
  void createOrder_whenVoyageCancelled_throwsException() {
    savedVoyage.setStatus(VoyageStatus.CANCELLED);
    voyageRepository.save(savedVoyage);

    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());

    assertThatThrownBy(() -> freightOrderService.createOrder(request))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("createOrder → throws when voyage not found")
  void createOrder_whenVoyageNotFound_throwsException() {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(999L);
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());

    assertThatThrownBy(() -> freightOrderService.createOrder(request))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
