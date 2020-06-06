# wow-server

1. Schema

```plantum
@startuml

box "Clients"
	actor "Game Client" as GC
end box

box "Login Server"
	participant "Login Server" as LS
end box

box "Game Servers"
    participant "Start Game Server Instance #0" as GS1
    participant "Game Server Instance #1" as GS2
    participant "Game Server Instance #2" as GS3
end box

box "Content Servers"
	participant "Arena Server" as AS
	participant "Dungeon Server" as DS
end box

box "Storage"
	participant "Postgres DB" as DB
	participant "Mongo DB" as MB
end box

box "Cloud Environment"
	participant "Amazon" as AMZ
end box

== GS Registration ==

GS1 -> AMZ : request tcp to LS
AMZ -> GS1 : return actual address
GS1 -> LS : connect via tcp
LS -> GS1 : accept init session
GS1 -> LS : register on LS
LS -> GS1 : confirm registration, prepare for incoming clients

== CS Registration Illustration ==

AS -> AMZ : request tcp to LS
AMZ -> AS : return actual address
AS -> LS : connect via tcp
LS -> AS : accept init session
AS -> LS : register request
LS -> GS1 : register CS on GS
GS1 -> AS : connect via tcp
AS -> GS1 : accept init session
GS1 -> LS : confirm registration

== Authorization ==

GC -> AMZ : request tcp to LS
AMZ -> GC : return actual address
GC -> LS : connect via tcp
LS -> GC : accept init session
GC -> LS : login
LS -> GC : login success
LS -> DB : load account
DB -> LS : return account
GC -> LS : request registered servers list
LS -> GC : servers list
GC -> LS : request enter selected GS
LS -> GC : pass account to selected GS, allow enter GS
GC -> GS2 : connect via tcp
GS2 -> GC : accept init session
GC -> GS2 : enter world, play

== Managing Game State ==

GS3 -> DB : update states
DB -> GS3 : request states

@enduml
```