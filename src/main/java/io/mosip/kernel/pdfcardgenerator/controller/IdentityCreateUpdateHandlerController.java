package io.mosip.kernel.pdfcardgenerator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.pdfcardgenerator.model.WebSubPushDataModel;
import io.mosip.kernel.pdfcardgenerator.service.spi.IdentityCreateUpdateHandlerService;
import io.mosip.kernel.pdfcardgenerator.service.spi.PdfCardGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest Controller To receive Identity Create/Update event from IDRepo Service through WebSub event. 
 * After receiving the request this service will initiate a call to credential request generator service
 * requesting for created or updated credentials.
 * 
 * @author Mahammed Taheer
 *
 * @since 1.2.1
 */

@SuppressWarnings("java:S5122") // Need CrossOrigin access for all the APIs, added to ignore in sonarCloud Security hotspots.
@CrossOrigin
@RestController
@Tag(name = "pdfcardgen", description = "Operation related to PDF Card Generation")
public class IdentityCreateUpdateHandlerController {
    
	 /**
	 * Identity Create/update handler Service to request latest credentials from ID Repo.
	 */
	@Autowired
	IdentityCreateUpdateHandlerService handlerService;

    /**
	 * Function to handle identity create/update event.
	 * 
	 * @param websubDataModel {@link WebSubPushDataModel} having required fields.
	 * @return 200 OK success reponse.
	 */
	@Operation(summary = "Function to sign response", description = "Function to sign response", tags = {
			"signaturecontroller" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@ResponseFilter
	@PostMapping(value = "/idCreateUpdateEventHandle")
	public ResponseEntity<String> idCreateUpdateEventHandle(@RequestBody WebSubPushDataModel websubDataModel) {
		String response = handlerService.handleIdentityEvent(websubDataModel);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
