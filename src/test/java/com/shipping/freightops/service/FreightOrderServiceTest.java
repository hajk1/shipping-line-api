package com.shipping.freightops.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.shipping.freightops.config.BookingProperties;
import com.shipping.freightops.dto.BookingStatusUpdateRequest;
import com.shipping.freightops.dto.CreateFreightOrderRequest;
import com.shipping.freightops.dto.UpdateDiscountRequest;
import com.shipping.freightops.entity.*;
import com.shipping.freightops.enums.AgentType;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.enums.VoyageStatus;
import com.shipping.freightops.exception.BadRequestException;
import com.shipping.freightops.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ExtendWith(OutputCaptureExtension.class)
public class FreightOrderServiceTest {
  @Autowired private FreightOrderService freightOrderService;
  @Autowired private VoyageRepository voyageRepository;
  @Autowired private ContainerRepository containerRepository;
  @Autowired private CustomerRepository customerRepository;
  @Autowired private PortRepository portRepository;
  @Autowired private VesselRepository vesselRepository;
  @Autowired private VoyagePriceRepository voyagePriceRepository;
  @Autowired private FreightOrderRepository freightOrderRepository;
  @Autowired private AgentRepository agentRepository;
  @Autowired private BookingProperties bookingProperties;
  @Autowired private VoyageService voyageService;

  private Voyage savedVoyage;
  private Container savedContainer;
  private Customer savedCustomer;
  private Agent savedAgent;

  @BeforeEach
  void setUp() {
    freightOrderRepository.deleteAll();
    voyagePriceRepository.deleteAll();
    voyageRepository.deleteAll();
    containerRepository.deleteAll();
    customerRepository.deleteAll();
    vesselRepository.deleteAll();
    portRepository.deleteAll();
    agentRepository.deleteAll();

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
    voyage.setMaxCapacityTeu(vessel.getCapacityTeu());
    voyage.setBookingOpen(true);
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

    savedAgent = new Agent();
    savedAgent.setActive(true);
    savedAgent.setName("Test Agent");
    savedAgent.setEmail("agent@somewhere.com");
    savedAgent.setType(AgentType.INTERNAL);
    savedAgent.setCommissionPercent(BigDecimal.TEN);
    agentRepository.save(savedAgent);
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
    request.setAgentId(savedAgent.getId());

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
    request.setAgentId(savedAgent.getId());

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
    request.setAgentId(savedAgent.getId());

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

  @Test
  @DisplayName("updateDiscount → updates discount and recalculates price")
  void updateDiscount_appliesCorrectly() {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("tester");
    request.setAgentId(savedAgent.getId());

    FreightOrder order = freightOrderService.createOrder(request);

    UpdateDiscountRequest update = new UpdateDiscountRequest();
    update.setDiscountPercent(BigDecimal.valueOf(11));
    update.setReason("Promo");

    FreightOrder updated = freightOrderService.updateDiscount(order.getId(), update);

    assertThat(updated.getDiscountPercent()).isEqualByComparingTo("11");
    assertThat(updated.getDiscountReason()).isEqualTo("Promo");
    assertThat(updated.getFinalPrice()).isEqualByComparingTo("890");
  }

  @Test
  @DisplayName("updateDiscount → throws when order is cancelled")
  void updateDiscount_whenCancelled_throws() {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("tester");
    request.setAgentId(savedAgent.getId());

    FreightOrder order = freightOrderService.createOrder(request);

    order.setStatus(OrderStatus.CANCELLED);
    freightOrderRepository.save(order);

    UpdateDiscountRequest update = new UpdateDiscountRequest();
    update.setDiscountPercent(BigDecimal.valueOf(10));
    update.setReason("Test");

    assertThatThrownBy(() -> freightOrderService.updateDiscount(order.getId(), update))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("updateDiscount → throws when order not found")
  void updateDiscount_notFound_throws() {
    UpdateDiscountRequest update = new UpdateDiscountRequest();
    update.setDiscountPercent(BigDecimal.valueOf(10));
    update.setReason("Test");

    assertThatThrownBy(() -> freightOrderService.updateDiscount(999L, update))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("updateDiscount → recalculates final price correctly for different values")
  void updateDiscount_recalculatesFinalPrice() {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("tester");
    request.setAgentId(savedAgent.getId());

    FreightOrder order = freightOrderService.createOrder(request);

    UpdateDiscountRequest update = new UpdateDiscountRequest();
    update.setReason("Test");

    // 10%
    update.setDiscountPercent(BigDecimal.valueOf(10));
    FreightOrder updated = freightOrderService.updateDiscount(order.getId(), update);
    assertThat(updated.getFinalPrice()).isEqualByComparingTo("900");

    // 25%
    update.setDiscountPercent(BigDecimal.valueOf(25));
    updated = freightOrderService.updateDiscount(order.getId(), update);
    assertThat(updated.getFinalPrice()).isEqualByComparingTo("750");

    // 0%
    update.setDiscountPercent(BigDecimal.ZERO);
    updated = freightOrderService.updateDiscount(order.getId(), update);
    assertThat(updated.getFinalPrice()).isEqualByComparingTo("1000");
  }

  @Test
  @DisplayName("updateDiscount → 100% discount results in zero price")
  void updateDiscount_fullDiscount() {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("tester");
    request.setAgentId(savedAgent.getId());

    FreightOrder order = freightOrderService.createOrder(request);

    UpdateDiscountRequest update = new UpdateDiscountRequest();
    update.setDiscountPercent(BigDecimal.valueOf(100));
    update.setReason("Free");

    FreightOrder updated = freightOrderService.updateDiscount(order.getId(), update);

    assertThat(updated.getFinalPrice()).isEqualByComparingTo("0");
  }

  @Test
  @DisplayName("createOrder → throws when booking is closed")
  void createOrder_whenBookingClosed_shouldThrowException() {
    savedVoyage.setBookingOpen(false);
    voyageRepository.save(savedVoyage);

    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setOrderedBy("tester");

    assertThatThrownBy(() -> freightOrderService.createOrder(request))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void createOrder_whenCapacityExceeded_shouldThrow() {
    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setAgentId(savedAgent.getId());
    request.setOrderedBy("test-user");

    savedVoyage.setMaxCapacityTeu(1); // small capacity
    voyageRepository.save(savedVoyage);

    freightOrderService.createOrder(request);

    assertThatThrownBy(() -> freightOrderService.createOrder(request))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("createOrder → triggers auto-cutoff when threshold reached")
  void createOrder_whenThresholdReached_shouldCloseBooking() {
    savedVoyage.setMaxCapacityTeu(5);
    savedVoyage.setBookingOpen(true);
    voyageRepository.save(savedVoyage);

    bookingProperties.setAutoCutoffPercent(50);

    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setAgentId(savedAgent.getId());
    request.setOrderedBy("tester");

    for (int i = 0; i < 3; i++) {
      freightOrderService.createOrder(request);
    }

    Voyage updatedVoyage = voyageRepository.findById(savedVoyage.getId()).orElseThrow();
    int currentLoadTeu = freightOrderRepository.sumTeuByVoyageId(savedVoyage.getId());

    assertThat(currentLoadTeu).isEqualTo(3);
    assertThat(updatedVoyage.isBookingOpen()).isFalse();
  }

  @Test
  @DisplayName("createOrder → rejects order if container exceeds remaining capacity")
  void createOrder_whenContainerExceedsRemainingCapacity_shouldThrow() {
    // Arrange
    savedVoyage.setMaxCapacityTeu(2);
    savedVoyage.setBookingOpen(true);
    voyageRepository.save(savedVoyage);

    CreateFreightOrderRequest request20ft = new CreateFreightOrderRequest();
    request20ft.setVoyageId(savedVoyage.getId());
    request20ft.setContainerId(savedContainer.getId()); // 1 TEU
    request20ft.setCustomerId(savedCustomer.getId());
    request20ft.setAgentId(savedAgent.getId());
    request20ft.setOrderedBy("tester");

    freightOrderService.createOrder(request20ft);

    Container container40ft =
        containerRepository.save(
            new Container("TSTU9999999", ContainerSize.FORTY_FOOT, ContainerType.DRY));

    VoyagePrice price = new VoyagePrice();
    price.setVoyage(savedVoyage);
    price.setContainerSize(ContainerSize.FORTY_FOOT);
    price.setBasePriceUsd(BigDecimal.valueOf(1500));
    voyagePriceRepository.save(price);

    CreateFreightOrderRequest request40ft = new CreateFreightOrderRequest();
    request40ft.setVoyageId(savedVoyage.getId());
    request40ft.setContainerId(container40ft.getId()); // 2 TEU
    request40ft.setCustomerId(savedCustomer.getId());
    request40ft.setAgentId(savedAgent.getId());
    request40ft.setOrderedBy("tester");

    assertThatThrownBy(() -> freightOrderService.createOrder(request40ft))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("createOrder → logs warning when auto-cutoff is triggered")
  void createOrder_whenAutoCutoffTriggered_shouldLogWarning(CapturedOutput output) {
    savedVoyage.setMaxCapacityTeu(2);
    savedVoyage.setBookingOpen(true);
    voyageRepository.save(savedVoyage);

    bookingProperties.setAutoCutoffPercent(50);

    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId());
    request.setCustomerId(savedCustomer.getId());
    request.setAgentId(savedAgent.getId());
    request.setOrderedBy("tester");

    freightOrderService.createOrder(request);

    assertThat(output.getAll())
        .contains("Auto cutoff triggered for voyage")
        .contains(savedVoyage.getId().toString())
        .contains("threshold: 50%");
  }

  @Test
  @DisplayName("createOrder → auto-cutoff triggers then booking can be reopened")
  void createOrder_autoCutoff_thenReopen_shouldWork() {
    savedVoyage.setMaxCapacityTeu(2);
    savedVoyage.setBookingOpen(true);
    voyageRepository.save(savedVoyage);

    bookingProperties.setAutoCutoffPercent(50); // cutoff at 1 TEU

    CreateFreightOrderRequest request = new CreateFreightOrderRequest();
    request.setVoyageId(savedVoyage.getId());
    request.setContainerId(savedContainer.getId()); // 1 TEU
    request.setCustomerId(savedCustomer.getId());
    request.setAgentId(savedAgent.getId());
    request.setOrderedBy("tester");

    freightOrderService.createOrder(request);

    Voyage updatedVoyage = voyageRepository.findById(savedVoyage.getId()).orElseThrow();
    assertThat(updatedVoyage.isBookingOpen()).isFalse();

    BookingStatusUpdateRequest reopenRequest = new BookingStatusUpdateRequest();
    reopenRequest.setBookingOpen(true);
    Voyage reopened = voyageService.updateBookingStatus(savedVoyage.getId(), reopenRequest);

    assertThat(reopened.isBookingOpen()).isTrue();

    freightOrderService.createOrder(request);
    int currentLoadTeu = freightOrderRepository.sumTeuByVoyageId(savedVoyage.getId());
    assertThat(currentLoadTeu).isEqualTo(2);
  }

  @Test
  @DisplayName("createOrder → rejects 40ft container when only 1 TEU remains")
  void createOrder_when40ftExceedsBut20ftFits_shouldThrowWithClearMessage() {
    savedVoyage.setMaxCapacityTeu(2);
    savedVoyage.setBookingOpen(true);
    voyageRepository.save(savedVoyage);

    CreateFreightOrderRequest request20ft = new CreateFreightOrderRequest();
    request20ft.setVoyageId(savedVoyage.getId());
    request20ft.setContainerId(savedContainer.getId()); // 20ft = 1 TEU
    request20ft.setCustomerId(savedCustomer.getId());
    request20ft.setAgentId(savedAgent.getId());
    request20ft.setOrderedBy("tester");

    freightOrderService.createOrder(request20ft);

    Container container40ft =
        containerRepository.save(
            new Container("TSTU9999999", ContainerSize.FORTY_FOOT, ContainerType.DRY));
    VoyagePrice price = new VoyagePrice();
    price.setVoyage(savedVoyage);
    price.setContainerSize(ContainerSize.FORTY_FOOT);
    price.setBasePriceUsd(BigDecimal.valueOf(1500));
    voyagePriceRepository.save(price);

    CreateFreightOrderRequest request40ft = new CreateFreightOrderRequest();
    request40ft.setVoyageId(savedVoyage.getId());
    request40ft.setContainerId(container40ft.getId());
    request40ft.setCustomerId(savedCustomer.getId());
    request40ft.setAgentId(savedAgent.getId());
    request40ft.setOrderedBy("tester");

    assertThatThrownBy(() -> freightOrderService.createOrder(request40ft))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Remaining capacity: 1 TEU")
        .hasMessageContaining("requires 2 TEU");
  }
}
