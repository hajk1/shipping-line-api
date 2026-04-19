package com.shipping.freightops.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.shipping.freightops.config.AppProperties;
import com.shipping.freightops.dto.ContainerLabelResponse;
import com.shipping.freightops.dto.CreateContainerRequest;
import com.shipping.freightops.entity.Container;
import com.shipping.freightops.entity.FreightOrder;
import com.shipping.freightops.enums.ContainerSize;
import com.shipping.freightops.enums.ContainerType;
import com.shipping.freightops.enums.OrderStatus;
import com.shipping.freightops.exception.PdfGenerationException;
import com.shipping.freightops.repository.ContainerRepository;
import com.shipping.freightops.repository.FreightOrderRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

record ContainerLabelData(
    String containerCode,
    String containerSize,
    String containerType,
    String voyageNumber,
    String vesselName,
    String departurePort,
    String arrivalPort,
    String departureDate,
    byte[] barcode,
    byte[] qrCode) {}

@Service
public class ContainerService {

  private final BarcodeService barcodeService;
  private final FreightOrderRepository freightOrderRepository;
  private final ContainerRepository containerRepository;
  private final AppProperties appProperties;
  private static final int BARCODE_WIDTH = 300;
  private static final int BARCODE_HEIGHT = 80;

  private static final int QR_WIDTH = 250;
  private static final int QR_HEIGHT = 250;

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public ContainerService(
      BarcodeService barcodeService,
      FreightOrderRepository freightOrderRepository,
      ContainerRepository containerRepository,
      AppProperties appProperties) {
    this.barcodeService = barcodeService;
    this.freightOrderRepository = freightOrderRepository;
    this.containerRepository = containerRepository;
    this.appProperties = appProperties;
  }

  @Transactional
  public Container createContainer(CreateContainerRequest request) {
    if (containerRepository.existsByContainerCode(request.getContainerCode())) {
      throw new IllegalStateException(
          "Container code already exists: " + request.getContainerCode());
    }

    Container container =
        new Container(request.getContainerCode(), request.getSize(), request.getType());
    return containerRepository.save(container);
  }

  @Transactional(readOnly = true)
  public List<Container> getAllContainers(ContainerSize size, ContainerType type) {
    if (size != null && type != null) {
      return containerRepository.findBySizeAndType(size, type);
    }
    if (size != null) {
      return containerRepository.findBySize(size);
    }
    if (type != null) {
      return containerRepository.findByType(type);
    }
    return containerRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Container getContainerById(Long id) {
    return containerRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Container not found: " + id));
  }

  @Transactional(readOnly = true)
  public ContainerLabelResponse generateContainerLabel(long containerId) {

    Container container = getContainer(containerId);

    ContainerLabelData data = buildLabelData(container);

    byte[] pdf = generatePdf(data);

    return new ContainerLabelResponse(pdf, "container-" + data.containerCode() + "-label.pdf");
  }

  private Container getContainer(long containerId) {
    return containerRepository
        .findById(containerId)
        .orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerId));
  }

  private byte[] generatePdf(ContainerLabelData data) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {

      Document document = new Document(PageSize.A6, 20, 20, 20, 20);

      PdfWriter.getInstance(document, out);
      document.open();

      Font bigBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
      Font keys = FontFactory.getFont(FontFactory.HELVETICA, 12);
      Font values = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
      Font small = FontFactory.getFont(FontFactory.HELVETICA, 10);

      addContainerHeader(document, data, bigBold);
      addContainerSpecs(document, data, keys, values);
      addVoyageInfo(document, data, keys, values);
      addQrSection(document, data, small);

      document.close();

    } catch (DocumentException | IOException e) {
      throw new PdfGenerationException("Failed to generate container label PDF", e);
    }
    return out.toByteArray();
  }

  private void addContainerHeader(Document document, ContainerLabelData data, Font title)
      throws DocumentException, IOException {

    Paragraph code = new Paragraph(data.containerCode(), title);
    code.setAlignment(Element.ALIGN_CENTER);
    document.add(code);

    Image barcodeImage = Image.getInstance(data.barcode());
    barcodeImage.setAlignment(Element.ALIGN_CENTER);
    barcodeImage.scaleToFit(180, 80);

    document.add(barcodeImage);
    document.add(new Paragraph("\n"));
  }

  private void addContainerSpecs(Document document, ContainerLabelData data, Font keys, Font values)
      throws DocumentException {
    PdfPTable containerSpecsTable = new PdfPTable(2);
    containerSpecsTable.setWidthPercentage(100);

    // Size
    Phrase sizePhrase = new Phrase();
    sizePhrase.add(new Chunk("Size: ", keys));
    sizePhrase.add(new Chunk(data.containerSize(), values));
    PdfPCell sizeCell = new PdfPCell(sizePhrase);
    sizeCell.setBorder(Rectangle.NO_BORDER);
    sizeCell.setPaddingBottom(4f);
    sizeCell.setHorizontalAlignment(Element.ALIGN_LEFT);

    // Type
    Phrase typePhrase = new Phrase();
    typePhrase.add(new Chunk("Type: ", keys));
    typePhrase.add(new Chunk(data.containerType(), values));
    PdfPCell typeCell = new PdfPCell(typePhrase);
    typeCell.setBorder(Rectangle.NO_BORDER);
    typeCell.setPaddingBottom(4f);
    typeCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

    containerSpecsTable.addCell(sizeCell);
    containerSpecsTable.addCell(typeCell);

    document.add(containerSpecsTable);
    document.add(new Paragraph("\n"));
  }

  private ContainerLabelData buildLabelData(Container container) {

    String containerCode = container.getContainerCode();

    byte[] barCode = barcodeService.generateBarcode(containerCode, BARCODE_WIDTH, BARCODE_HEIGHT);

    String containerSize = container.getSize().getPdfReadyValue();
    String containerType = container.getType().toString();

    String voyageNumber = "-";
    String vesselName = "-";
    String departurePort = "-";
    String arrivalPort = "-";
    String departureDate = "-";

    Optional<FreightOrder> activeOrderOpt =
        freightOrderRepository
            .findFirstByContainer_ContainerCodeAndStatusOrderByVoyage_DepartureTimeAsc(
                containerCode, OrderStatus.CONFIRMED);

    if (activeOrderOpt.isPresent()) {
      FreightOrder fo = activeOrderOpt.get();
      voyageNumber = fo.getVoyage().getVoyageNumber();
      vesselName = fo.getVoyage().getVessel().getName();
      departurePort = fo.getVoyage().getDeparturePort().getName();
      arrivalPort = fo.getVoyage().getArrivalPort().getName();
      departureDate = fo.getVoyage().getDepartureTime().format(DATE_FMT);
    }

    String trackUrl = appProperties.getBaseUrl() + "/api/v1/track/container/" + containerCode;

    byte[] qrCode = barcodeService.generateQrCode(trackUrl, QR_WIDTH, QR_HEIGHT);

    return new ContainerLabelData(
        containerCode,
        containerSize,
        containerType,
        voyageNumber,
        vesselName,
        departurePort,
        arrivalPort,
        departureDate,
        barCode,
        qrCode);
  }

  private void addVoyageInfo(Document document, ContainerLabelData data, Font keys, Font values)
      throws DocumentException {

    PdfPTable voyageTable = new PdfPTable(1);
    voyageTable.setWidthPercentage(100);

    // Voyage
    Phrase voyagePhrase = new Phrase();
    voyagePhrase.add(new Chunk("Voyage: ", keys));
    voyagePhrase.add(new Chunk(data.voyageNumber(), values));
    PdfPCell voyageCell = new PdfPCell(voyagePhrase);
    voyageCell.setBorder(Rectangle.NO_BORDER);
    voyageCell.setPaddingBottom(4f);
    voyageTable.addCell(voyageCell);

    // Vessel
    Phrase vesselPhrase = new Phrase();
    vesselPhrase.add(new Chunk("Vessel: ", keys));
    vesselPhrase.add(new Chunk(data.vesselName(), values));
    PdfPCell vesselCell = new PdfPCell(vesselPhrase);
    vesselCell.setBorder(Rectangle.NO_BORDER);
    vesselCell.setPaddingBottom(4f);
    voyageTable.addCell(vesselCell);

    // Route
    Phrase routePhrase = new Phrase();
    routePhrase.add(new Chunk(data.departurePort(), values));
    routePhrase.add(new Chunk(" -> ", keys));
    routePhrase.add(new Chunk(data.arrivalPort(), values));
    PdfPCell routeCell = new PdfPCell(routePhrase);
    routeCell.setBorder(Rectangle.NO_BORDER);
    routeCell.setPaddingBottom(4f);
    voyageTable.addCell(routeCell);

    // Departure
    Phrase depPhrase = new Phrase();
    depPhrase.add(new Chunk("Departure: ", keys));
    depPhrase.add(new Chunk(data.departureDate(), values));
    PdfPCell depCell = new PdfPCell(depPhrase);
    depCell.setBorder(Rectangle.NO_BORDER);
    depCell.setPaddingBottom(4f);
    voyageTable.addCell(depCell);

    document.add(voyageTable);
    document.add(new Paragraph("\n"));
  }

  private void addQrSection(Document document, ContainerLabelData data, Font small)
      throws DocumentException, IOException {

    PdfPTable qrTable = new PdfPTable(new float[] {1, 1});
    qrTable.setWidthPercentage(100);

    Image qrImage = Image.getInstance(data.qrCode());
    qrImage.scaleToFit(120, 120);
    qrImage.setBorder(Rectangle.NO_BORDER);

    PdfPCell qrCell = new PdfPCell(qrImage);
    qrCell.setBorder(Rectangle.NO_BORDER);
    qrCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    qrCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
    qrTable.addCell(qrCell);

    PdfPCell textCell = new PdfPCell(new Phrase("Scan to track", small));
    textCell.setBorder(Rectangle.NO_BORDER);
    textCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    textCell.setPaddingLeft(10f);
    qrTable.addCell(textCell);

    document.add(qrTable);
  }
}
