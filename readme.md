# Getting Started

### note: đây chỉ chạy api, cài đặt còn cấu hình(sql,redis,...) khác cập nhật sau

### Yêu cầu jdk 21
cách cài jdk 21
b1: lên gg tìm từ khóa: Oracle JDK 21 download
b2: chọn Windows x64 Installer (.msi)
b3: cài chọn Next → Next → Install ,  nó sẽ cài vào C:\Program Files\Java\jdk-21
b4: Mở Environment Variables
b4.1: System variables chọn New
b4.2: Name: JAVA_HOME còn Value: C:\Program Files\Java\jdk-21
b4.3: Update PATH , Trong System variables → Path → Edit: %JAVA_HOME%\bin , xong bấm ok-> apply
b5: mở cmd gõ java -version => java version 21.x.x là OK


### Cách chạy dự án
b1: clone dự án trên github desktop
b2: Mở terminal tại thư mục dự án và chạy lệnh => ./mvnw clean install(cài thư viện)
b3: muốn Truy cập Swagger UI  hoặc API tại: `http://localhost:8181/swagger-ui/index.html#/`