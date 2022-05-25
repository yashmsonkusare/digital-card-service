package io.mosip.kernel.pdfcardgenerator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.pdfcardgenerator.service.spi.PdfCardGeneratorService;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest Controller for PDF Card Service. Service includes consuming idrepo event, requesting credential data & 
 * sharing RID, Credential generation ID mapping.
 * 
 * @author Mahammed Taheer
 *
 * @since 1.2.1
 */
@SuppressWarnings("java:S5122") // Need CrossOrigin access for all the APIs, added to ignore in sonarCloud Security hotspots.
@CrossOrigin
@RestController
@Tag(name = "pdfcardgen", description = "Operation related to PDF Card Generation")
public class PdfCardGeneratorController {
    
    
    /**
	 * PDF Card Generator Service field with functions related to Card Generation.
	 */
	@Autowired
	PdfCardGeneratorService generatorService;

	
}
