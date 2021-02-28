package pl.aticode.civilworkoffers.unit.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pl.aticode.civilworkoffers.controller.EstimateController;
import pl.aticode.civilworkoffers.entity.estimate.Estimate;
import pl.aticode.civilworkoffers.entity.estimate.EstimateItem;
import pl.aticode.civilworkoffers.entity.estimate.TypeOfCivilWork;
import pl.aticode.civilworkoffers.entity.estimate.UnitOfMeasurement;
import pl.aticode.civilworkoffers.entity.offer.OfferRequest;
import pl.aticode.civilworkoffers.entity.user.Employee;
import pl.aticode.civilworkoffers.service.CharFilterService;
import pl.aticode.civilworkoffers.service.HibernateSearchService;
import pl.aticode.civilworkoffers.service.OfferService;
import pl.aticode.civilworkoffers.service.UserService;

class EstimateControllerTest {
	
    private MockMvc mockMvc;

    @InjectMocks
    private EstimateController estimateController;
    @Mock
    private OfferService offerService;
    @Mock
    private HibernateSearchService searchService;
    @Mock
    private UserService userService;
    @Mock
    private CharFilterService charFilterService;

	@BeforeEach
	void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(estimateController).build();
	}

	@Test
	void testEstimateDetailsOfferRequestModel() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
        mockMvc.perform(MockMvcRequestBuilders.get("/estimate/details")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateDetails"));
	}

	@Test
	void testEstimateDetailsOfferRequestIntegerModel() throws Exception {
		Estimate estimate = new Estimate();
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		Integer detachEstimateIdx = 0;
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/details")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("detachEstimateIdx", detachEstimateIdx.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateDetails"))
		        .andExpect(MockMvcResultMatchers.model().attribute("message", "estimate.detach.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).updateOfferRequestEstimateDetach(offerRequest, estimate);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/details")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("detachEstimateIdx", detachEstimateIdx.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateDetails"))
		        .andExpect(MockMvcResultMatchers.model().attribute("message", "estimate.detach.defeat"));
	}

	@Test
	void testNewEstimateModel() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/estimate/new"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/newEstimate"));
	}

	@Test
	void testNewEstimateStringLongLongStringOfferRequestRedirectAttributesModel() throws Exception {
		String searchEstimate = "search";
		Long estimateIdToCopy = 11L;
		Long estimateIdToConnect = 22L;
		String estimateData = "data1;data2";
		OfferRequest offerRequest = new OfferRequest();
		List<Estimate> orphanEstimateList = new ArrayList<>();
		Mockito.when(offerService.getOrphanEstimatesEmployee()).thenReturn(orphanEstimateList);
		List<Estimate> estimateList = new ArrayList<>();
		Mockito.when(searchService.searchEstimateByDescription(estimateData)).thenReturn(estimateList);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/new")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("searchEstimate", searchEstimate)
        		.param("estimateIdToCopy", estimateIdToCopy.toString())
        		.param("estimateIdToConnect", estimateIdToConnect.toString())
        		.param("estimateData", estimateData))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/newEstimate"))
		        .andExpect(MockMvcResultMatchers.model().attribute("orphanEstimateList", orphanEstimateList))
		        .andExpect(MockMvcResultMatchers.model().attribute("estimateList", estimateList));
        
		searchEstimate = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/new")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("searchEstimate", searchEstimate)
        		.param("estimateIdToCopy", estimateIdToCopy.toString())
        		.param("estimateIdToConnect", estimateIdToConnect.toString())
        		.param("estimateData", estimateData))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.copy.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).copyEstimate(offerRequest, estimateIdToCopy);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/new")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("searchEstimate", searchEstimate)
        		.param("estimateIdToCopy", estimateIdToCopy.toString())
        		.param("estimateIdToConnect", estimateIdToConnect.toString())
        		.param("estimateData", estimateData))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.copy.defeat"));
        
        estimateIdToCopy = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/new")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("searchEstimate", searchEstimate)
        		.param("estimateIdToConnect", estimateIdToConnect.toString())
        		.param("estimateData", estimateData))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.connect.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).updateOfferRequestEstimateConnect(offerRequest, estimateIdToConnect);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/new")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("searchEstimate", searchEstimate)
        		.param("estimateIdToConnect", estimateIdToConnect.toString())
        		.param("estimateData", estimateData))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.connect.defeat"));
        
        estimateIdToConnect = null;
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/new")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("searchEstimate", searchEstimate)
        		.param("estimateData", estimateData))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/newEstimate"));
	}

	@Test
	void testEstimateTemplateOfferRequestModel() throws Exception {
		List<Estimate> estimateList = new ArrayList<>();
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		Mockito.when(offerService.getOfferRequestInitializeEstimate(offerRequest)).thenReturn(offerRequest);
		List<TypeOfCivilWork> allTypeOfCivilWorkList = new ArrayList<>(); 
		Mockito.when(offerService.getAllTypeOfCivilWork()).thenReturn(allTypeOfCivilWorkList);
		List<UnitOfMeasurement> allUnitOfMeasurementList = new ArrayList<>(); 
		Mockito.when(offerService.getAllUnitOfMeasurement()).thenReturn(allUnitOfMeasurementList);
        mockMvc.perform(MockMvcRequestBuilders.get("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"))
		        .andExpect(MockMvcResultMatchers.model().attribute("estimateIdx", offerRequest.getEstimate().size() - 1))
		        .andExpect(MockMvcResultMatchers.model().attribute("allTypeOfCivilWorkList", allTypeOfCivilWorkList))
		        .andExpect(MockMvcResultMatchers.model().attribute("allUnitOfMeasurementList", allUnitOfMeasurementList));
	}

	@Test
	void testEstimateTemplateOfferRequestBindingResultStringStringArrayIntegerIntegerStringStringLongRedirectAttributesModel() throws Exception {
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setId(11L);
		String typeOfCivilWorkInput = "type";
		String[] unitOfMeasurementArray = {"unit"};
		Integer selectBox = 0;
		Integer estimateIdx = 0;
		String selectOption = "";
		Long itemIdBox = 0L;
		
		Employee loggedEmployee = new Employee();
		Mockito.when(userService.getLoggedEmployee()).thenReturn(loggedEmployee);
		
		List<TypeOfCivilWork> allTypeOfCivilWorkList = new ArrayList<>(); 
		Mockito.when(offerService.getAllTypeOfCivilWork()).thenReturn(allTypeOfCivilWorkList);
		List<UnitOfMeasurement> allUnitOfMeasurementList = new ArrayList<>(); 
		Mockito.when(offerService.getAllUnitOfMeasurement()).thenReturn(allUnitOfMeasurementList);
		
		String estimateItemDescription = "rem";
		selectOption = "searchEstimateItem";
		List<EstimateItem> estimateItemList = new ArrayList<>();
		Mockito.when(searchService.searchEstimateItemByDescription(estimateItemDescription)).thenReturn(estimateItemList);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"))
		        .andExpect(MockMvcResultMatchers.model().attribute("estimateItemList", estimateItemList));
        
        selectOption = "copyItem";
		ArgumentCaptor<OfferRequest> offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		ArgumentCaptor<Integer> estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Long> itemIdBoxCaptor = ArgumentCaptor.forClass(Long.class);
		doNothing().when(offerService).updateEstimateCopyItem(offerRequestCaptor.capture(), estimateIdxCaptor.capture(), itemIdBoxCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
		assertEquals(11, offerRequestCaptor.getValue().getId());
        
        selectOption = "saveEstimate";
		offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Employee> loggedEmployeeCaptor = ArgumentCaptor.forClass(Employee.class);
		doNothing().when(offerService).updateOfferRequestSaveEstimate(offerRequestCaptor.capture(), estimateIdxCaptor.capture(), loggedEmployeeCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
		assertEquals(11, offerRequestCaptor.getValue().getId());
		
        selectOption = "addItem";
		offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		doNothing().when(offerService).updateEstimateAddItem(offerRequestCaptor.capture(), estimateIdxCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
		assertEquals(11, offerRequestCaptor.getValue().getId());
        
        selectOption = "insertItemAbove";
		offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Integer> selectBoxCaptor = ArgumentCaptor.forClass(Integer.class);
		doNothing().when(offerService).updateEstimateInsertItemAbove(offerRequestCaptor.capture(), estimateIdxCaptor.capture(), selectBoxCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
		assertEquals(11, offerRequestCaptor.getValue().getId());
        
        selectOption = "insertItemBelow";
		offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		selectBoxCaptor = ArgumentCaptor.forClass(Integer.class);
		doNothing().when(offerService).updateEstimateInsertItemBelow(offerRequestCaptor.capture(), estimateIdxCaptor.capture(), selectBoxCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
		assertEquals(11, offerRequestCaptor.getValue().getId());
        
        selectOption = "moveUp";
		offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		selectBoxCaptor = ArgumentCaptor.forClass(Integer.class);
		doNothing().when(offerService).updateEstimateMoveItemUp(offerRequestCaptor.capture(), estimateIdxCaptor.capture(), selectBoxCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
		assertEquals(11, offerRequestCaptor.getValue().getId());
        
        selectOption = "moveDown";
		offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		selectBoxCaptor = ArgumentCaptor.forClass(Integer.class);
		doNothing().when(offerService).updateEstimateMoveItemDown(offerRequestCaptor.capture(), estimateIdxCaptor.capture(), selectBoxCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
		assertEquals(11, offerRequestCaptor.getValue().getId());
		
		typeOfCivilWorkInput = "ab";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.create.defeat"));
        
		typeOfCivilWorkInput = "abc<>";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.create.defeat"));
        
        typeOfCivilWorkInput = "abc";
        unitOfMeasurementArray = new String[] {""};
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.create.defeat"));
        
        unitOfMeasurementArray = new String[] {"<"};
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.create.defeat"));
        
        selectOption = "removeItem";
        typeOfCivilWorkInput = "abc";
        unitOfMeasurementArray = new String[] {"m"};
		offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		selectBoxCaptor = ArgumentCaptor.forClass(Integer.class);
		doNothing().when(offerService).updateEstimateRemoveItem(offerRequestCaptor.capture(), estimateIdxCaptor.capture(), selectBoxCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"));
		assertEquals(11, offerRequestCaptor.getValue().getId());
        
		typeOfCivilWorkInput = "abc<>";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.create.defeat"));
        
        typeOfCivilWorkInput = "abc";
        unitOfMeasurementArray = new String[] {"m", ""};
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.create.defeat"));
        
        unitOfMeasurementArray = new String[] {"<"};
        selectBox = 1;
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/details"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.create.defeat"));
        
        selectOption = "0";
		offerRequestCaptor = ArgumentCaptor.forClass(OfferRequest.class);
		estimateIdxCaptor = ArgumentCaptor.forClass(Integer.class);
		loggedEmployeeCaptor = ArgumentCaptor.forClass(Employee.class);
		doNothing().when(offerService).updateOfferRequestSaveEstimate(offerRequestCaptor.capture(), estimateIdxCaptor.capture(), loggedEmployeeCaptor.capture());
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/template")
        		.sessionAttr("offerRequest", offerRequest)
        		.param("typeOfCivilWork", typeOfCivilWorkInput)
        		.param("unitOfMeasurement", unitOfMeasurementArray)
        		.param("selectBox", selectBox.toString())
        		.param("estimateIdx", estimateIdx.toString())
        		.param("selectOption", selectOption)
        		.param("estimateItemDescription", estimateItemDescription)
        		.param("itemIdBox", itemIdBox.toString())
        		.sessionAttr("estimateIdx", estimateIdx))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateTemplate"))
		        .andExpect(MockMvcResultMatchers.model().attribute("estimateIdx", selectOption));
		assertEquals(11, offerRequestCaptor.getValue().getId());
	}

	@Test
	void testShowEstimateOfferRequestIntegerModel() throws Exception {
		Estimate estimate = new Estimate();
		estimate.setId(33L);
		List<Estimate> estimateList = new ArrayList<>();
		estimateList.add(estimate);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setEstimate(estimateList);
		offerRequest.setId(11L);
		Mockito.when(offerService.getOfferRequestInitializeEstimate(offerRequest)).thenReturn(offerRequest);
        mockMvc.perform(MockMvcRequestBuilders.get("/estimate/show/0")
        		.sessionAttr("offerRequest", offerRequest))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/showEstimate"))
		        .andExpect(MockMvcResultMatchers.model().attribute("estimate", estimate));
	}

	@Test
	void testShowEstimateLongModel() throws Exception {
		Long estimateIdToShow = 13L;
		Estimate estimate = new Estimate();
		Mockito.when(offerService.getEstimate(estimateIdToShow)).thenReturn(estimate);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/show")
        		.param("estimateIdToShow", estimateIdToShow.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/showEstimate"))
		        .andExpect(MockMvcResultMatchers.model().attribute("estimate", estimate));
	}

	@Test
	void testEstimateService() throws Exception {
		List<TypeOfCivilWork> allTypeOfCivilWorkEmployee = new ArrayList<>(); 
		Mockito.when(offerService.getAllTypeOfCivilWorkEmployee()).thenReturn(allTypeOfCivilWorkEmployee);
		List<UnitOfMeasurement> allUnitOfMeasurementEmployee = new ArrayList<>(); 
		Mockito.when(offerService.getAllUnitOfMeasurementEmployee()).thenReturn(allUnitOfMeasurementEmployee);
        mockMvc.perform(MockMvcRequestBuilders.get("/estimate/service"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateService"))
		        .andExpect(MockMvcResultMatchers.model().attribute("allTypeOfCivilWorkEmployee", allTypeOfCivilWorkEmployee))
		        .andExpect(MockMvcResultMatchers.model().attribute("allUnitOfMeasurementEmployee", allUnitOfMeasurementEmployee));
		
	}

	@Test
	void testTypeOfCivilWorkServiceModel() throws Exception {
		List<TypeOfCivilWork> allTypeOfCivilWorkEmployee = new ArrayList<>(); 
		Mockito.when(offerService.getAllTypeOfCivilWorkEmployee()).thenReturn(allTypeOfCivilWorkEmployee);
        mockMvc.perform(MockMvcRequestBuilders.get("/estimate/service/type"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/typeOfCWService"))
		        .andExpect(MockMvcResultMatchers.model().attributeHasNoErrors("typeOfCivilWork"))
		        .andExpect(MockMvcResultMatchers.model().attribute("allTypeOfCivilWorkEmployee", allTypeOfCivilWorkEmployee));
	}

	@Test
	void testTypeOfCivilWorkServiceTypeOfCivilWorkBindingResultStringLongModelRedirectAttributes() throws Exception {
		String swichValue = "";
		Long typeOfCivilWorkId = 22L;
		TypeOfCivilWork typeOfCivilWork = new TypeOfCivilWork();
		typeOfCivilWork.setName("name");
		List<TypeOfCivilWork> typeOfCivilWorkList = new ArrayList<>();
		typeOfCivilWorkList.add(typeOfCivilWork);
		Mockito.when(offerService.getTypeOfCivilWork(typeOfCivilWork.getName())).thenReturn(typeOfCivilWorkList);
		List<TypeOfCivilWork> allTypeOfCivilWorkEmployee = new ArrayList<>(); 
		Mockito.when(offerService.getAllTypeOfCivilWorkEmployee()).thenReturn(allTypeOfCivilWorkEmployee);
		
		swichValue = "addTypeOfCivilWork";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/type")
        		.flashAttr("typeOfCivilWork", typeOfCivilWork)
        		.param("swichValue", swichValue)
        		.param("typeOfCivilWorkId", typeOfCivilWorkId.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/typeOfCWService"));
        
        typeOfCivilWork.setName("name1");
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/type")
        		.flashAttr("typeOfCivilWork", typeOfCivilWork)
        		.param("swichValue", swichValue)
        		.param("typeOfCivilWorkId", typeOfCivilWorkId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.type.add.success"));
        
		Mockito.doThrow(new Exception("TEST")).when(offerService).addTypeOfCivilWork(typeOfCivilWork);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/type")
        		.flashAttr("typeOfCivilWork", typeOfCivilWork)
        		.param("swichValue", swichValue)
        		.param("typeOfCivilWorkId", typeOfCivilWorkId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.type.add.defeat"));
        
        swichValue = "deleteTypeOfCivilWork";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/type")
        		.flashAttr("typeOfCivilWork", typeOfCivilWork)
        		.param("swichValue", swichValue)
        		.param("typeOfCivilWorkId", typeOfCivilWorkId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.type.delete.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).deleteTypeOfCivilWork(typeOfCivilWorkId);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/type")
        		.flashAttr("typeOfCivilWork", typeOfCivilWork)
        		.param("swichValue", swichValue)
        		.param("typeOfCivilWorkId", typeOfCivilWorkId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.type.delete.defeat"));
	}

	@Test
	void testUnitOfMeasurementServiceModel() throws Exception {
		List<UnitOfMeasurement> allUnitOfMeasurementEmployee = new ArrayList<>(); 
		Mockito.when(offerService.getAllUnitOfMeasurementEmployee()).thenReturn(allUnitOfMeasurementEmployee);
        mockMvc.perform(MockMvcRequestBuilders.get("/estimate/service/unit"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.view().name("estimate/unitOfMeasureService"))
        .andExpect(MockMvcResultMatchers.model().attributeHasNoErrors("unitOfMeasurement"))
        .andExpect(MockMvcResultMatchers.model().attribute("allUnitOfMeasurementEmployee", allUnitOfMeasurementEmployee));
		
	}

	@Test
	void testUnitOfMeasurementServiceUnitOfMeasurementBindingResultStringLongModelRedirectAttributes() throws Exception {
		String swichValue = "";
		Long unitOfMeasurementId = 22L;
		UnitOfMeasurement unitOfMeasurement = new UnitOfMeasurement();
		unitOfMeasurement.setName("name");
		List<UnitOfMeasurement> unitOfMeasurementList = new ArrayList<>();
		unitOfMeasurementList.add(unitOfMeasurement);
		Mockito.when(offerService.getUnitOfMeasurement(unitOfMeasurement.getName())).thenReturn(unitOfMeasurementList);
		List<UnitOfMeasurement> allUnitOfMeasurementEmployee = new ArrayList<>(); 
		Mockito.when(offerService.getAllUnitOfMeasurementEmployee()).thenReturn(allUnitOfMeasurementEmployee);
		
		swichValue = "addUnitOfMeasurement";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/unit")
        		.flashAttr("unitOfMeasurement", unitOfMeasurement)
        		.param("swichValue", swichValue)
        		.param("unitOfMeasurementId", unitOfMeasurementId.toString()))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/unitOfMeasureService"));
        
        unitOfMeasurement.setName("name1");
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/unit")
        		.flashAttr("unitOfMeasurement", unitOfMeasurement)
        		.param("swichValue", swichValue)
        		.param("unitOfMeasurementId", unitOfMeasurementId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.unit.add.success"));
        
		Mockito.doThrow(new Exception("TEST")).when(offerService).addUnitOfMeasurement(unitOfMeasurement);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/unit")
        		.flashAttr("unitOfMeasurement", unitOfMeasurement)
        		.param("swichValue", swichValue)
        		.param("unitOfMeasurementId", unitOfMeasurementId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.unit.add.defeat"));
        
        swichValue = "deleteUnitOfMeasurement";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/unit")
        		.flashAttr("unitOfMeasurement", unitOfMeasurement)
        		.param("swichValue", swichValue)
        		.param("unitOfMeasurementId", unitOfMeasurementId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.unit.delete.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).deleteUnitOfMeasurement(unitOfMeasurementId);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/unit")
        		.flashAttr("unitOfMeasurement", unitOfMeasurement)
        		.param("swichValue", swichValue)
        		.param("unitOfMeasurementId", unitOfMeasurementId.toString()))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.unit.delete.defeat"));
	}

	@Test
	void testDeleteEstimateService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/estimate/service/delete"))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateDeleteService"));
	}

	@Test
	void testDeleteEstimateServiceLongStringModelRedirectAttributes() throws Exception {
		Long estimateId = 55L;
		String swichValue = "";
		List<Estimate> orphanEstimatesEmployeeList = new ArrayList<>(); 
		Mockito.when(offerService.getOrphanEstimatesEmployee()).thenReturn(orphanEstimatesEmployeeList);
		
		swichValue = "searchEstimate";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/delete")
        		.param("estimateId", estimateId.toString())
        		.param("swichValue", swichValue))
		        .andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.view().name("estimate/estimateDeleteService"))
		        .andExpect(MockMvcResultMatchers.model().attribute("orphanEstimatesEmployeeList", orphanEstimatesEmployeeList));
        
        swichValue = "deleteEstimate";
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/delete")
        		.param("estimateId", estimateId.toString())
        		.param("swichValue", swichValue))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.delete.success"));
        
        Mockito.doThrow(new Exception("TEST")).when(offerService).deleteEstimate(estimateId);
        mockMvc.perform(MockMvcRequestBuilders.post("/estimate/service/delete")
        		.param("estimateId", estimateId.toString())
        		.param("swichValue", swichValue))
		        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
		        .andExpect(MockMvcResultMatchers.view().name("redirect:/estimate/service"))
		        .andExpect(MockMvcResultMatchers.flash().attribute("message", "estimate.service.delete.defeat"));
	}

}
