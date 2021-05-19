package at.srfg.iot.aas.service.types;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import at.srfg.iot.aas.repository.registry.AssetAdministrationShellRepository;
import at.srfg.iot.aas.repository.registry.ConceptDescriptionRepository;
import at.srfg.iot.aas.repository.registry.IdentifiableRepository;
import at.srfg.iot.aas.repository.registry.SubmodelElementRepository;
import at.srfg.iot.aas.repository.registry.SubmodelRepository;
import at.srfg.iot.common.datamodel.asset.aas.basic.Asset;
import at.srfg.iot.common.datamodel.asset.aas.basic.AssetAdministrationShell;
import at.srfg.iot.common.datamodel.asset.aas.basic.GlobalReference;
import at.srfg.iot.common.datamodel.asset.aas.basic.Identifier;
import at.srfg.iot.common.datamodel.asset.aas.basic.Submodel;
import at.srfg.iot.common.datamodel.asset.aas.common.referencing.Kind;
import at.srfg.iot.common.datamodel.asset.aas.common.types.CategoryEnum;
import at.srfg.iot.common.datamodel.asset.aas.common.types.DataTypeEnum;
import at.srfg.iot.common.datamodel.asset.aas.common.types.DirectionEnum;
import at.srfg.iot.common.datamodel.asset.aas.dictionary.ConceptDescription;
import at.srfg.iot.common.datamodel.asset.aas.modeling.submodelelement.EventElement;
import at.srfg.iot.common.datamodel.asset.aas.modeling.submodelelement.Operation;
import at.srfg.iot.common.datamodel.asset.aas.modeling.submodelelement.OperationVariable;
import at.srfg.iot.common.datamodel.asset.aas.modeling.submodelelement.Property;
import at.srfg.iot.common.datamodel.asset.aas.modeling.submodelelement.SubmodelElementCollection;

@Service
public class DefaultAssetType {
	public static final Identifier ASSET_TYPE_AAS = new Identifier( "http://iasset.salzburgresearch.at/labor/belt#aas");
	public static final Identifier ASSET_TYPE_IDENTIFIER = new Identifier( "http://iasset.salzburgresearch.at/labor/belt");
	public static final Identifier ASSET_TYPE_INFO_MODEL = new Identifier( "http://iasset.salzburgresearch.at/labor/belt#info");
	public static final Identifier ASSET_TYPE_PROPERTY_MODEL = new Identifier( "http://iasset.salzburgresearch.at/labor/belt#properties");
	public static final Identifier ASSET_TYPE_OPERATION_MODEL = new Identifier( "http://iasset.salzburgresearch.at/labor/belt#operations");
	public static final Identifier ASSET_TYPE_EVENT_MODEL = new Identifier( "http://iasset.salzburgresearch.at/labor/belt#events");
	
	public static final Identifier APPLICATION_TYPE_EVENT_MODEL			= new Identifier("http://iasset.salzburgresearch.at/registry/application#events");

	public static final String ASSET_TYPE_CATEGORY = "iAssetLabor";
	@Autowired
	protected AssetAdministrationShellRepository aasRepo;
	@Autowired
	protected SubmodelRepository aasSubmodelRepo;
	@Autowired
	protected SubmodelElementRepository aasSubmodelElementRepo;
	@Autowired
	protected ConceptDescriptionRepository cdRepo;
	@Autowired
	protected IdentifiableRepository<GlobalReference> globalReferenceRepo;

	@PostConstruct
	@Transactional
	protected void init() {
		Optional<AssetAdministrationShell> applicationTypeAAS = aasRepo.findByIdentification(ASSET_TYPE_AAS);
		AssetAdministrationShell theShell = applicationTypeAAS.orElseGet(new Supplier<AssetAdministrationShell>() {
			@Override
			public AssetAdministrationShell get() {
				Asset asset = new Asset(ASSET_TYPE_IDENTIFIER);
				asset.setDescription("de", "i-Asset Labor - Förderband");
				asset.setKind(Kind.Type);
				AssetAdministrationShell shell = new AssetAdministrationShell(asset);
				shell.setIdentification(ASSET_TYPE_AAS);
				shell.setAsset(asset);
				shell.setIdShort("beltType");
				shell.setCategory(CategoryEnum.APPLICATION_CLASS);
				shell.setDescription("de", "i-Asset Labor - Förderband");
				shell.setVersion("V0.01");
				shell.setRevision("001");
				// create the asset along with the 
				return aasRepo.save(shell);
			}});
		Optional<Submodel> assetTypeInfoModel = aasSubmodelRepo.findByIdentification(ASSET_TYPE_INFO_MODEL);
		Submodel infoModel = assetTypeInfoModel.orElseGet(new Supplier<Submodel>() {
			@Override
			public Submodel get() {
				Submodel model = new Submodel(ASSET_TYPE_INFO_MODEL, theShell);
				model.setIdShort("information");
				model.setKind(Kind.Instance);
				model.setCategory(CategoryEnum.APPLICATION_CLASS);
				model.setDescription("de", "i-Asset Labor - Förderband-Infomodel");
				model.setVersion("V0.01");
				model.setRevision("001");
				return aasSubmodelRepo.save(model);
			}
		});
		Optional<GlobalReference> optManufacturerReference = globalReferenceRepo.findByIdentification(new Identifier("0173-1#02-AAO677#002"));
		GlobalReference manufacturerName = optManufacturerReference.orElseGet(new Supplier<GlobalReference>() {
			// provide the sensorEvent type ... 
			@Override
			public GlobalReference get() {
				GlobalReference ref = new GlobalReference(new Identifier("0173-1#02-AAO677#002"));
				ref.setCategory(CategoryEnum.PROPERTY);
				ref.setIdShort("0173-1#02-AAO677#002");
				ref.setDescription("de", "Herstellername");
				return globalReferenceRepo.save(ref);
			}
		});

		// use a concept description 
		Optional<ConceptDescription> manufacturerDescription = cdRepo.findByIdentification(new Identifier("http://iasset.salzburgresearch.at/dictionary/manufacturer"));
		ConceptDescription manufacturer = manufacturerDescription.orElseGet(new Supplier<ConceptDescription>() {

			@Override
			public ConceptDescription get() {
				ConceptDescription manufacturer = new ConceptDescription(new Identifier("http://iasset.salzburgresearch.at/dictionary/manufacturer"));
				manufacturer.setCategory(CategoryEnum.PROPERTY);
				manufacturer.setDescription("en", "Manufacturer name");
				manufacturer.setDescription("de", "Hersteller Name");
//				manufacturer.setCategory("A21");
//				manufacturer.setVersion("001");
//				manufacturer.setRevision("01");
//				manufacturer.addCaseOf(manufacturerName);
				
				return cdRepo.save(manufacturer);
			}
		});
		// add the instance property for the manufacturer
		Optional<Property> optManufacturer = infoModel.getSubmodelElement("manufacturer", Property.class);
		Property manufacturerProperty = optManufacturer.orElseGet(new Supplier<Property>() {

			@Override
			public Property get() {
				// 
				Property property = new Property("manufacturer", infoModel);
				property.setCategory(CategoryEnum.CONSTANT);
				property.setKind(Kind.Instance);
				property.setDescription("de", "Hersteller");
				property.setSemanticElement(manufacturerName);
				// TODO: use enumeration for ValueQualifier
				property.setValueQualifier(DataTypeEnum.STRING);
				property.setValue("Hersteller Förderband");
				return aasSubmodelElementRepo.save(property);
			}
			
		});
		Optional<Submodel> assetTypePropertyModel = aasSubmodelRepo.findByIdentification(ASSET_TYPE_PROPERTY_MODEL);
		Submodel propertyModel = assetTypePropertyModel.orElseGet(new Supplier<Submodel>() {
			@Override
			public Submodel get() {
				Submodel model = new Submodel(ASSET_TYPE_PROPERTY_MODEL, theShell);
				model.setIdShort("properties");
				model.setKind(Kind.Type);
				model.setCategory(CategoryEnum.APPLICATION_CLASS);
				model.setDescription("de", "i-Asset Labor - Förderband-Properties");
				model.setVersion("V0.01");
				model.setRevision("001");
				return aasSubmodelRepo.save(model);
			}
		});
		Optional<SubmodelElementCollection> optBeltDataContainer = propertyModel.getSubmodelElement("beltData", SubmodelElementCollection.class);
		SubmodelElementCollection beltDataContainer = optBeltDataContainer.orElseGet(new Supplier<SubmodelElementCollection>() {

			@Override
			public SubmodelElementCollection get() {
				SubmodelElementCollection dataContainer = new SubmodelElementCollection("beltData", propertyModel);
				dataContainer.setDescription("de", "Förderband Daten" );
				dataContainer.setKind(Kind.Type);
				
				return aasSubmodelElementRepo.save(dataContainer);
			}
		});
		Optional<Property> optBeltState = beltDataContainer.getSubmodelElement("state", Property.class);
		Property beltState = optBeltState.orElseGet(new Supplier<Property>() {

			@Override
			public Property get() {
				// 
				Property property = new Property("state", beltDataContainer);
				property.setCategory(CategoryEnum.VARIABLE);
				property.setKind(Kind.Type);
				property.setDescription("de", "Status des Förderbands");
				property.setSemanticElement(manufacturer);
				// TODO: use enumeration for ValueQualifier
				property.setValueQualifier(DataTypeEnum.STRING);
				return aasSubmodelElementRepo.save(property);
			}
			
		});
		Optional<Property> optBeltMoving = beltDataContainer.getSubmodelElement("moving", Property.class);
		Property beltMoving = optBeltState.orElseGet(new Supplier<Property>() {

			@Override
			public Property get() {
				// 
				Property property = new Property("moving", beltDataContainer);
				property.setCategory(CategoryEnum.VARIABLE);
				property.setKind(Kind.Type);
				property.setDescription("de", "In Begwegung (J/N)");
				// property.setSemanticElement(manufacturer);
				// TODO: use enumeration for ValueQualifier
				property.setValueQualifier(DataTypeEnum.BOOLEAN);
				return aasSubmodelElementRepo.save(property);
			}
			
		});
		Optional<Property> optServerTime = beltDataContainer.getSubmodelElement("serverTime", Property.class);
		Property serverTime = optServerTime.orElseGet(new Supplier<Property>() {

			@Override
			public Property get() {
				// 
				Property property = new Property("serverTime", beltDataContainer);
				property.setCategory(CategoryEnum.VARIABLE);
				property.setKind(Kind.Type);
				property.setDescription("de", "Server Laufzeit");
				//property.setSemanticElement(manufacturer);
				property.setValueQualifier(DataTypeEnum.STRING);
				return aasSubmodelElementRepo.save(property);
			}
			
		});
		
		Optional<Property> optBeltDirection = beltDataContainer.getSubmodelElement("direction", Property.class);
		Property beltDirection = optBeltDirection.orElseGet(new Supplier<Property>() {

			@Override
			public Property get() {
				// 
				Property property = new Property("direction", beltDataContainer);
				property.setCategory(CategoryEnum.VARIABLE);
				property.setKind(Kind.Type);
				property.setDescription("de", "Bewegungsrichtung");
				//property.setSemanticElement(manufacturer);
				property.setValueQualifier(DataTypeEnum.STRING);
				return aasSubmodelElementRepo.save(property);
			}
			
		});
		Optional<Property> optBeltDistance = beltDataContainer.getSubmodelElement("distance", Property.class);
		Property beltDistance = optBeltDistance.orElseGet(new Supplier<Property>() {

			@Override
			public Property get() {
				// 
				Property property = new Property("distance", beltDataContainer);
				property.setCategory(CategoryEnum.VARIABLE);
				property.setKind(Kind.Type);
				property.setDescription("de", "Zurückgelegte Distanz");
				property.setValueQualifier(DataTypeEnum.DECIMAL);
				// TODO: add semantic ID
				return aasSubmodelElementRepo.save(property);
			}
			
		});
		Optional<Submodel> assetTypeOperationModel = aasSubmodelRepo.findByIdentification(ASSET_TYPE_OPERATION_MODEL);
		Submodel operationModel = assetTypeOperationModel.orElseGet(new Supplier<Submodel>() {
			@Override
			public Submodel get() {
				Submodel model = new Submodel(ASSET_TYPE_OPERATION_MODEL, theShell);
				model.setIdShort("operations");
				model.setKind(Kind.Type);
				model.setCategory(CategoryEnum.APPLICATION_CLASS);
				model.setDescription("de", "i-Asset Labor - Förderband-Funktionen");
				model.setVersion("V0.01");
				model.setRevision("001");
				return aasSubmodelRepo.save(model);
			}
		});
		
//		Optional<Property> optBeltSpeed = beltDataContainer.getSubmodelElement("speed", Property.class);
//		Property beltSpeed = optBeltSpeed.orElseGet(new Supplier<Property>() {
//
//			@Override
//			public Property get() {
//				// 
//				Property property = new Property("speed", beltDataContainer);
//				property.setCategory(CategoryEnum.VARIABLE);
//				property.setKind(Kind.Type);
//				property.setDescription("de", "Geschwindigkeit für Förderband");
//				property.setValueQualifier(DataTypeEnum.DECIMAL);
//				return aasSubmodelElementRepo.save(property);
//			}
//			
//		});
		Optional<Operation> optSwitchLight = operationModel.getSubmodelElement("switchBusyLight", Operation.class);
		Operation switchLight = optSwitchLight.orElseGet(new Supplier<Operation>() {
			@Override
			public Operation get() {
				Operation m = new Operation("switchBusyLight", operationModel);
				m.setCategory(CategoryEnum.FUNCTION);
				m.setDescription("de", "Geschwindigkeit für Förderband einstellen");
				m.setKind(Kind.Type);
				
				// the output variable is specified with the CMMS instance!
				return aasSubmodelElementRepo.save(m);
			}
		});
		Optional<OperationVariable> optStateVariable = switchLight.getOperationVariable("state");
		OperationVariable stateVariable = optStateVariable.orElseGet(new Supplier<OperationVariable>() {
			@Override
			public OperationVariable get() {
				OperationVariable v = new OperationVariable("state", switchLight, DirectionEnum.Input);
				v.setKind(Kind.Type);
				v.setDescription("de", "Ein/Aus Status für Kontroll-Lampe" );
				
				// use the property as the value
				v.setValue(beltState);
				// 
				
				return aasSubmodelElementRepo.save(v);
			}
		});
		Optional<Operation> optMoveBelt = operationModel.getSubmodelElement("moveBelt", Operation.class);
		Operation moveBelt = optSwitchLight.orElseGet(new Supplier<Operation>() {
			@Override
			public Operation get() {
				Operation m = new Operation("moveBelt", operationModel);
				m.setCategory(CategoryEnum.FUNCTION);
				m.setDescription("de", "Band vor/zurück bewegen");
				m.setKind(Kind.Type);
				
				// the output variable is specified with the CMMS instance!
				return aasSubmodelElementRepo.save(m);
			}
		});
		Optional<OperationVariable> optDirectionVariable = moveBelt.getOperationVariable("direction");
		OperationVariable directionVariable = optStateVariable.orElseGet(new Supplier<OperationVariable>() {
			@Override
			public OperationVariable get() {
				OperationVariable v = new OperationVariable("direction", moveBelt, DirectionEnum.Input);
				v.setKind(Kind.Type);
				v.setDescription("de", "Bewegungsrichtung" );
				// add semantic element from semantic lookup
				v.setValue(beltDirection);
				// 
				
				return aasSubmodelElementRepo.save(v);
			}
		});
		Optional<OperationVariable> optDistanceVariable = moveBelt.getOperationVariable("distance");
		OperationVariable distanceVariable = optStateVariable.orElseGet(new Supplier<OperationVariable>() {
			@Override
			public OperationVariable get() {
				OperationVariable v = new OperationVariable("distance", moveBelt, DirectionEnum.Input);
				v.setKind(Kind.Type);
				v.setDescription("de", "Zurückzulegende Entfernung" );
				
				// use the property as the value
				v.setValue(beltDistance);
				// 
				
				return aasSubmodelElementRepo.save(v);
			}
		});

		Optional<Submodel> assetTypeEventModel = aasSubmodelRepo.findByIdentification(ASSET_TYPE_EVENT_MODEL);
		Submodel eventModel = assetTypeEventModel.orElseGet(new Supplier<Submodel>() {
			@Override
			public Submodel get() {
				Submodel model = new Submodel(ASSET_TYPE_EVENT_MODEL, theShell);
				model.setIdShort("events");
				model.setKind(Kind.Type);
				model.setCategory(CategoryEnum.APPLICATION_CLASS);
				model.setDescription("de", "i-Asset Labor - Förderband-Ereignisse");
				model.setVersion("V0.01");
				model.setRevision("001");
				return aasSubmodelRepo.save(model);
			}
		});
		
		Optional<EventElement> optStateEvent = eventModel.getSubmodelElement("stateEvent", EventElement.class);
		EventElement stateEvent = optStateEvent.orElseGet(new Supplier<EventElement>() {

			@Override
			public EventElement get() {
				EventElement m = new EventElement("beltData", eventModel);
				m.setCategory(CategoryEnum.EVENT);
				m.setDescription("de", "Status-Informationen für Förderband");
				m.setKind(Kind.Type);
				m.setObservableElement(beltDataContainer);
				m.setMessageTopic("belt.data.topic");
				m.setDirection(DirectionEnum.Output);
				// the output variable is specified with the CMMS instance!
				return aasSubmodelElementRepo.save(m);
			}
		});
		Optional<Submodel> appEventSubmodel = aasSubmodelRepo.findByIdentification(APPLICATION_TYPE_EVENT_MODEL);
		if ( appEventSubmodel.isPresent()) {
			Submodel eventSubmodel = appEventSubmodel.get();
			Optional<SubmodelElementCollection> mBroker = eventSubmodel.getSubmodelElement("messageBroker", SubmodelElementCollection.class);
			if ( mBroker.isPresent()) {
				stateEvent.setMessageBrokerElement(mBroker.get());
				aasSubmodelElementRepo.save(stateEvent);
			}
		}
	}

}
