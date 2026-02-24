# Data Pipeline Service - Postman Testing Guide

## 🚀 Quick Start (Service Already Running in IntelliJ)

Since your service is **already running in IntelliJ IDEA**, you can start testing immediately!

## Service Information

- **Base URL**: `http://localhost:8082`
- **Service**: Data Pipeline Service (Consolidated)
- **Port**: 8082
- **Status**: ✅ RUNNING (IntelliJ IDEA)

---

## Prerequisites Status Check

### ✅ Required Services

1. **PostgreSQL** (localhost:5432)
   - Database: `mypolicy_db`
   - Username: `postgres`
   - Password: `root` ⚠️ (Updated from postgres123)
   - Status: Check with `psql -U postgres -d mypolicy_db`

2. **MongoDB** (localhost:27017)
   - Database: `ingestion_db`
   - Collections: `ingestion_jobs`, `audit_logs`
   - Status: Check with `mongo --eval "db.version()"`
   - **CRITICAL**: MongoDB MUST be running or service won't start!

3. **JWT Token** (Optional for initial testing)
   - Health endpoints work WITHOUT JWT ✅
   - Upload/Protected endpoints need JWT 🔒

---

## 🔍 Step-by-Step Testing (Start Here!)

### STEP 1: Verify Service is Running ⚡

Open Postman and test the health endpoint (NO JWT NEEDED):

```
GET http://localhost:8082/api/v1/ingestion/health
```

**Expected Response:**

```
Ingestion Module: OK - Ready to process files
```

✅ If you get this response, your service is running perfectly!

---

## 📋 Complete API Testing Guide

### STEP 2: Test All Health Endpoints (No JWT Required)

Test each module's health endpoint to verify all services are working:

#### 2.1 Ingestion Health Check ✅

```http
GET http://localhost:8082/api/v1/ingestion/health
```

**Expected Response:**

```
Ingestion Module: OK - Ready to process files
```

#### 2.2 Metadata Health Check ✅

```http
GET http://localhost:8082/api/v1/metadata/health
```

**Expected Response:**

```
Metadata Module: OK
```

#### 2.3 Processing Health Check ✅

```http
GET http://localhost:8082/api/v1/processing/health
```

**Expected Response:**

```
Processing Module: OK
```

---

### STEP 3: Setup Insurer Configuration (Metadata)

Before uploading files, you MUST configure the insurer field mappings.

#### 3.1 Create HDFC Life Configuration

**Request:**

```http
POST http://localhost:8082/api/v1/metadata/config?insurerId=HDFC_LIFE&insurerName=HDFC%20Life%20Insurance
Content-Type: application/json

{
  "TERM_LIFE": [
    {
      "sourceField": "Policy_No",
      "targetField": "policyNumber",
      "dataType": "STRING",
      "required": true,
      "transformRule": "uppercase",
      "defaultValue": null
    },
    {
      "sourceField": "Insured_Name",
      "targetField": "customerName",
      "dataType": "STRING",
      "required": true,
      "transformRule": "trim",
      "defaultValue": null
    },
    {
      "sourceField": "Email",
      "targetField": "email",
      "dataType": "STRING",
      "required": true,
      "transformRule": "lowercase",
      "defaultValue": null
    },
    {
      "sourceField": "Mobile",
      "targetField": "mobileNumber",
      "dataType": "STRING",
      "required": true,
      "transformRule": "normalize_mobile",
      "defaultValue": null
    },
    {
      "sourceField": "Premium_Amt",
      "targetField": "premiumAmount",
      "dataType": "DECIMAL",
      "required": true,
      "transformRule": "parse_number",
      "defaultValue": "0.00"
    },
    {
      "sourceField": "Sum_Assured",
      "targetField": "sumAssured",
      "dataType": "DECIMAL",
      "required": true,
      "transformRule": "parse_number",
      "defaultValue": "0.00"
    },
    {
      "sourceField": "Start_Date",
      "targetField": "startDate",
      "dataType": "DATE",
      "required": true,
      "transformRule": "parse_date",
      "defaultValue": null
    },
    {
      "sourceField": "Maturity_Date",
      "targetField": "endDate",
      "dataType": "DATE",
      "required": false,
      "transformRule": "parse_date",
      "defaultValue": null
    }
  ]
}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Configuration saved successfully",
  "data": {
    "insurerId": "HDFC_LIFE",
    "insurerName": "HDFC Life Insurance",
    "totalMappings": 8
  },
  "timestamp": "2026-02-24T..."
}
```

#### 3.2 Verify Configuration Was Saved

**Request:**

```http
GET http://localhost:8082/api/v1/metadata/config/HDFC_LIFE
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Configuration retrieved successfully",
  "data": {
    "insurerId": "HDFC_LIFE",
    "insurerName": "HDFC Life Insurance",
    "active": true,
    "fileFormat": "EXCEL",
    "fieldMappings": {
      "TERM_LIFE": [
        {
          "sourceField": "Policy_No",
          "targetField": "policyNumber",
          ...
        }
      ]
    },
    "createdAt": "2026-02-24T...",
    "updatedAt": "2026-02-24T..."
  }
}
```

---

### STEP 4: Testing WITHOUT JWT (Temporary for Development)

For initial testing, you can test without JWT. However, in production, JWT is REQUIRED.

**Option A: Skip JWT Validation (Development Only)**

- Comment out the JWT filter in SecurityConfig temporarily
- Restart service

**Option B: Use Sample Excel Without Upload Endpoint**

- Manually place test file in `./uploads` folder
- Note the file path
- Use Processing endpoint directly (see Step 6)

---

### STEP 5: Upload File (Requires JWT Token)

⚠️ **This endpoint requires JWT authentication**

#### 5.1 Get JWT Token First

To get a JWT token, you need to:

1. **Start customer-service** (port 8081)
2. **Register a user:**

   ```http
   POST http://localhost:8081/api/v1/customers/register
   Content-Type: application/json

   {
     "email": "testuser@example.com",
     "password": "Test@1234",
     "fullName": "Test User",
     "mobileNumber": "9876543210"
   }
   ```

3. **Login to get token:**

   ```http
   POST http://localhost:8081/api/v1/customers/login
   Content-Type: application/json

   {
     "email": "testuser@example.com",
     "password": "Test@1234"
   }
   ```

   **Response:**

   ```json
   {
     "success": true,
     "message": "Login successful",
     "data": {
       "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTcwODc3NjAwMCwiZXhwIjoxNzA4ODYyNDAwfQ.abc123...",
       "email": "testuser@example.com",
       "fullName": "Test User"
     }
   }
   ```

   **Copy the token value!** ✅

#### 5.2 Upload File with JWT

**Request:**

```http
POST http://localhost:8082/api/v1/ingestion/upload
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTcwODc3NjAwMCwiZXhwIjoxNzA4ODYyNDAwfQ.abc123...
Content-Type: multipart/form-data

Form Data:
- file: [Select your Excel/CSV file - see sample below]
- insurerId: HDFC_LIFE
```

**In Postman:**

1. Select `POST` method
2. URL: `http://localhost:8082/api/v1/ingestion/upload`
3. **Headers** tab:
   - Key: `Authorization`
   - Value: `Bearer YOUR_TOKEN_HERE`
4. **Body** tab → Select `form-data`:
   - Key: `file` (change type to `File`)
   - Value: Click "Select Files" and choose your Excel file
   - Key: `insurerId` (type: `Text`)
   - Value: `HDFC_LIFE`

**Expected Response:**

```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "fileName": "HDFC_Policies_Feb2026.xlsx",
    "fileSize": 45678,
    "insurerId": "HDFC_LIFE",
    "status": "UPLOADED",
    "totalRecords": null,
    "processedRecords": 0,
    "uploadedBy": "testuser@example.com",
    "uploadedAt": "2026-02-24T10:30:00"
  },
  "timestamp": "2026-02-24T10:30:00"
}
```

**✅ Save the `jobId` - you'll need it for the next steps!**

---

### STEP 6: Check Job Status

After uploading, check the processing status.

**Request:**

```http
GET http://localhost:8082/api/v1/ingestion/status/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

Replace `a1b2c3d4-e5f6-7890-abcd-ef1234567890` with your actual jobId from upload response.

**Expected Response (UPLOADED status):**

```json
{
  "success": true,
  "message": "Job status retrieved successfully",
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "fileName": "HDFC_Policies_Feb2026.xlsx",
    "status": "UPLOADED",
    "totalRecords": 100,
    "processedRecords": 0,
    "failedRecords": 0,
    "insurerId": "HDFC_LIFE",
    "uploadedBy": "testuser@example.com",
    "uploadedAt": "2026-02-24T10:30:00",
    "startedAt": null,
    "completedAt": null,
    "failureReason": null
  }
}
```

**Expected Response (PROCESSING status):**

```json
{
  "success": true,
  "message": "Job status retrieved successfully",
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "fileName": "HDFC_Policies_Feb2026.xlsx",
    "status": "PROCESSING",
    "totalRecords": 100,
    "processedRecords": 45,
    "failedRecords": 0,
    "insurerId": "HDFC_LIFE",
    "uploadedBy": "testuser@example.com",
    "uploadedAt": "2026-02-24T10:30:00",
    "startedAt": "2026-02-24T10:30:15",
    "completedAt": null,
    "failureReason": null
  }
}
```

---

### STEP 7: Automatic Processing (Scheduled)

The **FileProcessingScheduler** automatically processes uploaded files every 30 seconds.

**What happens automatically:**

1. Scheduler checks for jobs with status `UPLOADED`
2. Triggers `ProcessingService.processFile()`
3. Updates status to `PROCESSING`
4. Parses Excel/CSV file
5. Applies field mappings and transformations
6. Validates data
7. Calls MatchingService to link customers/policies
8. Updates status to `COMPLETED` or `FAILED`
9. Creates audit logs in MongoDB

**Check logs in IntelliJ console:**

```
[Scheduler] Processing job: a1b2c3d4-e5f6-7890-abcd-ef1234567890
[Processing] Starting file processing: jobId=a1b2c3d4-e5f6-7890-abcd-ef1234567890
[Processing] Fetching metadata configuration for insurerId=HDFC_LIFE
[Processing] Found 8 field mappings for policyType=TERM_LIFE
[Processing] Parsing Excel file...
[Processing] Processing record 1/100
[Processing] Record validated successfully
[Matching] Searching for customer with email: john.doe@example.com
[Processing] Job completed: 100 records processed, 0 failed
```

---

### STEP 8: Manual Processing Trigger (Optional)

You can also manually trigger processing:

**Request:**

```http
POST http://localhost:8082/api/v1/processing/trigger?jobId=a1b2c3d4-e5f6-7890-abcd-ef1234567890&policyType=TERM_LIFE
```

**Expected Response:**

```
Processing started for jobId: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

---

## 📊 Sample Test Data Files

### Sample Excel File (HDFC_Policies_Feb2026.xlsx)

Create an Excel file with these columns:

| Policy_No | Insured_Name | Email | Mobile | Premium_Amt | Sum_Assured | Start_Date | Maturity_Date |
|-----------|--------------|-------|--------|-------------|-------------|------------|---------------|
| POL001 | John Doe | john.doe@example.com | 9876543210 | 25,000 | 10,00,000 | 01/01/2026 | 01/01/2046 |
| POL002 | Jane Smith | jane.smith@example.com | 9876543211 | 30,000 | 15,00,000 | 15/01/2026 | 15/01/2046 |
| POL003 | Bob Johnson | bob.j@example.com | 9876543212 | 28,000 | 12,00,000 | 01/02/2026 | 01/02/2046 |
| POL004 | Alice Williams | alice.w@example.com | 9876543213 | 35,000 | 20,00,000 | 10/02/2026 | 10/02/2046 |
| POL005 | Charlie Brown | charlie.b@example.com | 9876543214 | 22,000 | 8,00,000 | 15/02/2026 | 15/02/2046 |

**Important Notes:**
- Excel file should have **header row** with exact column names as configured in metadata
- Dates can be in multiple formats: `dd/MM/yyyy`, `MM/dd/yyyy`, `yyyy-MM-dd`
- Numbers can have commas (₹25,000) - they will be parsed automatically
- Mobile numbers will be normalized (spaces/hyphens removed)
- Email will be converted to lowercase

### Sample CSV File (policies.csv)

```csv
Policy_No,Insured_Name,Email,Mobile,Premium_Amt,Sum_Assured,Start_Date,Maturity_Date
POL001,John Doe,john.doe@example.com,9876543210,25000,1000000,01/01/2026,01/01/2046
POL002,Jane Smith,jane.smith@example.com,9876543211,30000,1500000,15/01/2026,15/01/2046
POL003,Bob Johnson,bob.j@example.com,9876543212,28000,1200000,01/02/2026,01/02/2046
```

---

## 🔧 Postman Collection Setup

### Environment Variables Setup

1. **Create New Environment** in Postman
2. **Name**: `MyPolicy - Data Pipeline (Local)`
3. **Add Variables**:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `baseUrl` | `http://localhost:8082` | `http://localhost:8082` |
| `customerServiceUrl` | `http://localhost:8081` | `http://localhost:8081` |
| `jwtToken` | (leave empty) | (will be set after login) |
| `jobId` | (leave empty) | (will be set from upload) |
| `insurerId` | `HDFC_LIFE` | `HDFC_LIFE` |

### Auto-Save JobId from Upload Response

In Postman, for the **Upload File** request:

1. Go to **Tests** tab
2. Add this script:

```javascript
// Auto-save jobId to environment variable
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.success && jsonData.data.jobId) {
        pm.environment.set("jobId", jsonData.data.jobId);
        console.log("JobId saved: " + jsonData.data.jobId);
    }
}
```

### Auto-Save JWT Token from Login

For the **Login** request (customer-service):

1. Go to **Tests** tab
2. Add this script:

```javascript
// Auto-save JWT token
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.success && jsonData.data.token) {
        pm.environment.set("jwtToken", jsonData.data.token);
        console.log("JWT Token saved!");
    }
}
```

---

## 🎯 Complete Testing Flow (Recommended Order)

### ✅ Flow 1: First Time Setup

1. **Start MongoDB** ← **CRITICAL**
   ```powershell
   net start MongoDB
   # OR
   mongod --dbpath="C:\data\db"
   ```

2. **Verify Service Running**
   ```http
   GET {{baseUrl}}/api/v1/ingestion/health
   ```

3. **Create Insurer Configuration**
   ```http
   POST {{baseUrl}}/api/v1/metadata/config?insurerId={{insurerId}}&insurerName=HDFC%20Life
   (with JSON body from Step 3.1 above)
   ```

4. **Verify Configuration**
   ```http
   GET {{baseUrl}}/api/v1/metadata/config/{{insurerId}}
   ```

### ✅ Flow 2: File Upload & Processing (With JWT)

1. **Start customer-service** (port 8081)

2. **Register User** (First time only)
   ```http
   POST {{customerServiceUrl}}/api/v1/customers/register
   ```

3. **Login to Get Token**
   ```http
   POST {{customerServiceUrl}}/api/v1/customers/login
   ```
   → Token auto-saves to `{{jwtToken}}`

4. **Upload File**
   ```http
   POST {{baseUrl}}/api/v1/ingestion/upload
   Authorization: Bearer {{jwtToken}}
   ```
   → JobId auto-saves to `{{jobId}}`

5. **Check Status (Wait 30 seconds)**
   ```http
   GET {{baseUrl}}/api/v1/ingestion/status/{{jobId}}
   ```

6. **Check Status Again** (Status should be PROCESSING or COMPLETED)
   ```http
   GET {{baseUrl}}/api/v1/ingestion/status/{{jobId}}
   ```

### ✅ Flow 3: Manual Processing Trigger

1. **Trigger Processing**
   ```http
   POST {{baseUrl}}/api/v1/processing/trigger?jobId={{jobId}}&policyType=TERM_LIFE
   ```

2. **Monitor Progress**
   ```http
   GET {{baseUrl}}/api/v1/ingestion/status/{{jobId}}
   ```
   Keep calling this every 5-10 seconds to see progress

---

## 🐛 Troubleshooting Guide

### Error 1: Connection Refused (Unable to Connect)

**Symptoms:**
```
Could not send request
Error: connect ECONNREFUSED 127.0.0.1:8082
```

**Causes & Solutions:**
- ❌ Service not running → ✅ Check IntelliJ console - should see "Started DataPipelineApplication"
- ❌ Wrong port → ✅ Verify service is on port 8082 (check application.yaml)
- ❌ Firewall blocking → ✅ Temporarily disable firewall or add exception

### Error 2: MongoDB Connection Failed

**Symptoms (in IntelliJ console):**
```
com.mongodb.MongoSocketOpenException: Exception opening socket
Failed to connect to localhost:27017
```

**Solution:**
```powershell
# Check if MongoDB is running
net start MongoDB

# OR start manually
mongod --dbpath="C:\data\db"

# Verify MongoDB is accessible
mongo --eval "db.version()"
```

**After fixing MongoDB:**
- ✅ Restart the service in IntelliJ (Stop → Debug/Run again)

### Error 3: MongoTemplate Bean Not Found

**Symptoms:**
```
Parameter 0 of constructor in AuditService required a bean named 'mongoTemplate' that could not be found
```

**Solution:**
- ✅ We already fixed this! (Removed autoconfigure.exclude)
- ✅ Make sure MongoDB is running BEFORE starting the service
- ✅ Restart IntelliJ IDEA service

### Error 4: 401 Unauthorized

**Symptoms:**
```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/ingestion/upload"
}
```

**Causes & Solutions:**
- ❌ Missing JWT token → ✅ Add `Authorization: Bearer {{jwtToken}}` header
- ❌ Expired token → ✅ Login again to get fresh token
- ❌ Invalid token → ✅ Verify token format: `Bearer eyJhbGciOi...`

### Error 5: 400 Bad Request - Missing required field

**Symptoms:**
```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Required field 'Policy_No' is missing or null"
  }
}
```

**Solution:**
- ✅ Check Excel file has all required columns
- ✅ Verify column names match metadata configuration EXACTLY
- ✅ Check for empty rows in Excel (remove them)

### Error 6: 404 Not Found - Configuration not found

**Symptoms:**
```json
{
  "success": false,
  "message": "Configuration not found for insurerId: HDFC_LIFE"
}
```

**Solution:**
- ✅ Create configuration first (Step 3.1)
- ✅ Verify insurerId matches exactly (case-sensitive!)
- ✅ Check PostgreSQL is running and accessible

### Error 7: File Upload Failed - File too large

**Symptoms:**
```json
{
  "timestamp": "2026-02-24T10:30:00",
  "status": 413,
  "error": "Payload Too Large",
  "message": "Maximum upload size exceeded"
}
```

**Solution:**
- ✅ File size limit is 50MB (configured in application.yaml)
- ✅ Reduce file size or split into multiple files
- ✅ Check `spring.servlet.multipart.max-file-size` in application.yaml

### Error 8: Processing Failed - Invalid Date Format

**Symptoms (in logs):**
```
java.time.format.DateTimeParseException: Text '32/13/2026' could not be parsed
```

**Solution:**
- ✅ Use valid date formats: `dd/MM/yyyy`, `MM/dd/yyyy`, `yyyy-MM-dd`
- ✅ Check for typos in date values (32/13 is invalid)
- ✅ Dates should not be empty if field is required

---

## 📝 IntelliJ Console Logs to Watch

When testing, watch the IntelliJ console for these log entries:

### ✅ Successful Flow Logs:

```
[Scheduler] Found 1 uploaded files to process
[Scheduler] Processing job: a1b2c3d4-e5f6-7890-abcd-ef1234567890
[Processing] Starting file processing: jobId=a1b2c3d4-e5f6-7890-abcd-ef1234567890, insurerId=HDFC_LIFE, policyType=TERM_LIFE
[Processing] Fetching metadata configuration for insurerId=HDFC_LIFE
[Processing] Found 8 field mappings for policyType=TERM_LIFE
[Processing] Excel file contains 100 data rows
[Processing] Processing record 1/100
[Matching] Searching for customer: john.doe@example.com, mobile: 9876543210
[Matching] Customer found: CUSTOMER-123
[Processing] Record 1 processed successfully
...
[Processing] Processing record 100/100
[Processing] Processing completed: 100 records processed, 0 failed
[Audit] Audit log created: FILE_PROCESSING - a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### ❌ Error Flow Logs:

```
[Scheduler] Processing job: a1b2c3d4-e5f6-7890-abcd-ef1234567890
[Processing] Starting file processing: jobId=a1b2c3d4-e5f6-7890-abcd-ef1234567890
[Processing] ERROR: Configuration not found for insurerId=HDFC_LIFE
[Processing] Processing failed for jobId=a1b2c3d4-e5f6-7890-abcd-ef1234567890
[Audit] Audit log created: FILE_PROCESSING - FAILED: Configuration not found
```

---

## 🎓 Advanced Testing Scenarios

### Scenario 1: Test Field Transformations

Upload an Excel file with various data formats to test transformations:

| Policy_No | Email | Mobile | Premium_Amt |
|-----------|-------|--------|-------------|
| pol001 | JOHN.DOE@EXAMPLE.COM | +91 98765-43210 | ₹25,000.00 |

**Expected after transformation:**
- `policyNumber`: `POL001` (uppercase)
- `email`: `john.doe@example.com` (lowercase)
- `mobileNumber`: `9876543210` (normalized)
- `premiumAmount`: `25000.00` (parsed number)

### Scenario 2: Test Validation Errors

Upload file with invalid data:

| Policy_No | Email | Mobile | Premium_Amt |
|-----------|-------|--------|-------------|
| POL001 | invalid-email | 12345 | abc |

**Expected:**
- Validation should fail
- Status should be `FAILED`
- Check `failureReason` in job status

### Scenario 3: Test Automatic Retry

Upload a file that fails processing:
1. File processes and fails
2. Wait 1 hour
3. Scheduler automatically retries failed jobs
4. Check logs for `Retrying failed job: ...`

---

## 📦 Export Postman Collection

After setting up all requests, export your collection:

1. **File** → **Export Collection**
2. Save as: `MyPolicy-DataPipeline-Collection.json`
3. Share with team members

**Collection Structure:**
```
MyPolicy Data Pipeline
├── 🏥 Health Checks
│   ├── Ingestion Health
│   ├── Metadata Health
│   └── Processing Health
├── ⚙️ Metadata Management
│   ├── Create Configuration
│   └── Get Configuration
├── 👤 Authentication (Customer Service)
│   ├── Register User
│   └── Login (Get JWT)
├── 📤 File Upload & Processing
│   ├── Upload File
│   ├── Get Job Status
│   └── Trigger Manual Processing
└── 🧪 Advanced Testing
    ├── Update Progress (Internal)
    └── Update Status (Internal)
```

---

## 🚀 Quick Start Commands Summary

```powershell
# 1. Start MongoDB (CRITICAL!)
net start MongoDB

# 2. Verify PostgreSQL is running
psql -U postgres -d mypolicy_db

# 3. Your service is already running in IntelliJ ✅

# 4. Start customer-service (for JWT)
cd ../customer-service
mvn spring-boot:run

# 5. Open Postman and start testing! 🎯
```

---

## 📞 Need Help?

- ✅ Service logs: Check IntelliJ IDEA console
- ✅ Database issues: Check PostgreSQL (port 5432) and MongoDB (port 27017)
- ✅ JWT issues: Make sure customer-service is running on port 8081
- ✅ File format issues: Verify Excel headers match metadata configuration exactly

**Happy Testing! 🎉**
