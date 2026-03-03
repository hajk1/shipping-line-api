package com.shipping.freightops.repository;

import com.shipping.freightops.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {}
