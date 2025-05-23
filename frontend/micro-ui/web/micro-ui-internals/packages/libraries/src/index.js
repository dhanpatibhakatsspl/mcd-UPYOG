import i18next from "i18next";
import Enums from "./enums/index";
import mergeConfig from "./config/mergeConfig";
import { useStore } from "./services/index";
import { initI18n } from "./translations/index";
import { Storage, PersistantStorage } from "./services/atoms/Utils/Storage";
import { UploadServices } from "./services/atoms/UploadServices";
import JsDictionary from "./services/atoms/JsDictionary";
import AuditService from "./services/elements/Audit";
import { LocationService } from "./services/elements/Location";
import { LocalityService } from "./services/elements/Localities";
import { LocalizationService } from "./services/elements/Localization/service";
import { LoginService } from "./services/elements/Login";
import { PGRService } from "./services/elements/PGR";
import { FSMService } from "./services/elements/FSM";
import { PaymentService } from "./services/elements/Payment";
import * as dateUtils from "./services/atoms/Utils/Date";
import Download from "./services/atoms/Download";
import { WorkflowService } from "./services/elements/WorkFlow";
import { MdmsService } from "./services/elements/MDMS";
import { Complaint } from "./services/elements/Complaint";
import { UserService } from "./services/elements/User";
import { PTService } from "./services/elements/PT";
import { WSService } from "./services/elements/WS";
import { TLService } from "./services/elements/TL";
import { Surveys } from "./services/elements/Surveys";
import { MCollectService } from "./services/elements/MCollect";
import { ReportsService } from "./services/elements/Reports";
import HrmsService from "./services/elements/HRMS";
import { InboxGeneral } from "./services/elements/InboxService";
import EventsServices from "./services/elements/Events";
import { DigiLockerService } from "./services/elements/DigiLocker";

import ShareFiles from "./services/molecules/ShareFiles";
import { GetServiceDefinitions } from "./services/molecules/ServiceDefinitions";
import { ULBService } from "./services/molecules/Ulb";
import { FileDesludging } from "./services/molecules/FSM/FileDesludging";
import { ComponentRegistryService } from "./services/elements/ComponentRegistry";
import StoreData from "./services/molecules/StoreData";

import Contexts from "./contexts";
import Hooks from "./hooks";
import Utils from "./utils";
import { subFormRegistry } from "./subFormRegistry";
import ReceiptsService from "./services/elements/Receipts";
import { EDCRService } from "./services/elements/EDCR";
import { OBPSService } from "./services/elements/OBPS";
import { NOCService } from "./services/elements/NOC";
import { NOCSearch } from "./services/molecules/NOC/Search";
import AccessControlService from "./services/elements/Access";
import BillServices from "./services/elements/Bill";
import { CFService } from "./services/elements/CF";
import { PTRService } from "./services/elements/PTR";  
import { ASSETService } from "./services/elements/ASSET";  
import { CHBServices } from "./services/elements/CHB"; 
import { ADSServices } from "./services/elements/ADS";
import { SVService } from "./services/elements/SV";
import { EwService } from "./services/elements/EW";
import { EmployeeDashboardService } from "./services/elements/EMPDASHBOARD";
import {CustomService} from "./services/elements/CustomService";    
import { CMServices } from "./services/elements/CM";
import { WTService } from "./services/elements/WT";
import { VendorService } from "./services/elements/EmpVendor";
import { MTService } from "./services/elements/MT";
   


const setupLibraries = (Library, props) => {
  window.Digit = window.Digit || {};
  window.Digit[Library] = window.Digit[Library] || {};
  window.Digit[Library] = { ...window.Digit[Library], ...props };
};

const initLibraries = () => {
  setupLibraries("SessionStorage", Storage);
  setupLibraries("PersistantStorage", PersistantStorage);
  setupLibraries("UserService", UserService);
  setupLibraries("ULBService", ULBService);
  setupLibraries("Config", { mergeConfig });
  setupLibraries("Services", { useStore });
  setupLibraries("Enums", Enums);
  setupLibraries("LocationService", LocationService);
  setupLibraries("LocalityService", LocalityService);
  setupLibraries("LoginService", LoginService);
  setupLibraries("LocalizationService", LocalizationService);
  setupLibraries("PGRService", PGRService);
  setupLibraries("FSMService", FSMService);
  setupLibraries("PTService", PTService);
  setupLibraries("CFService", CFService);
  setupLibraries("CustomService", CustomService);
  setupLibraries("TLService", TLService);
  setupLibraries("DigiLockerService",DigiLockerService)
  setupLibraries("Surveys", Surveys);
  setupLibraries("HRMSService", HrmsService);
  setupLibraries("ReceiptsService", ReceiptsService);
  setupLibraries("MCollectService", MCollectService);
  setupLibraries("ReportsService", ReportsService)
  setupLibraries("PaymentService", PaymentService);
  setupLibraries("EDCRService", EDCRService);
  setupLibraries("OBPSService", OBPSService);
  setupLibraries("DateUtils", dateUtils);
  setupLibraries("WorkflowService", WorkflowService);
  setupLibraries("MDMSService", MdmsService);
  setupLibraries("UploadServices", UploadServices);
  setupLibraries("JsDictionary", JsDictionary);
  setupLibraries("GetServiceDefinitions", GetServiceDefinitions);
  setupLibraries("Complaint", Complaint);
  setupLibraries("FileDesludging", FileDesludging);
  setupLibraries("ComponentRegistryService", ComponentRegistryService);
  setupLibraries("StoreData", StoreData);
  setupLibraries("EventsServices", EventsServices);
  setupLibraries("WSService", WSService);

  setupLibraries("InboxGeneral", InboxGeneral);
  setupLibraries("ShareFiles", ShareFiles);
  setupLibraries("Contexts", Contexts);
  setupLibraries("Hooks", Hooks);
  setupLibraries("Customizations", {});
  setupLibraries("Utils", Utils);
  setupLibraries("Download", Download);

  setupLibraries("NOCService", NOCService);
  setupLibraries("NOCSearch", NOCSearch);
  setupLibraries("AccessControlService", AccessControlService);
  setupLibraries("BillServices", BillServices);
  setupLibraries("AuditService",AuditService);
  setupLibraries("PTRService", PTRService);
  setupLibraries("ASSETService", ASSETService);
  setupLibraries("CHBServices", CHBServices);
  setupLibraries("ADSServices", ADSServices);
  setupLibraries("EwService", EwService);
  setupLibraries("SVService", SVService);
  setupLibraries("CMServices", CMServices);
  setupLibraries("EmployeeDashboardService", EmployeeDashboardService);
  setupLibraries("WTService",WTService);
  setupLibraries("VendorService", VendorService);
  setupLibraries("MTService", MTService);

  return new Promise((resolve) => {
    initI18n(resolve);
  });
};

export { initLibraries, Enums, Hooks, subFormRegistry };
