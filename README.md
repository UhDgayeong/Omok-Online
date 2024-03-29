# Omok-Online


이 프로젝트는 컴퓨터공학부 네트워크 프로그래밍 수업에서 Java 소켓 프로그래밍 실습을 위해 개발한 1인 개발 텀프로젝트입니다. </br>
서버를 실행한 후, 해당 서버에 접속한 클라이언트들은 게임방을 생성하거나 게임방에 참여하여 오목 게임을 할 수 있습니다.

## 목차
1. [기능 설명 및 시연](#기능-설명-및-시연)
2. [개발 기획](#개발-기획)
3. [메소드 흐름 예시](#메소드-흐름-예시)
4. [개선할 점](#개선할-점)
5. [기타 사항](#기타-사항)
</br>

---
</br>

## 기능 설명 및 시연
| 서버 실행, 접속 | 방 생성 |
|:--------------:|:------:|
|![녹화_2023_02_05_02_31_50_698](https://user-images.githubusercontent.com/96832560/216781404-79c99e27-6eda-4730-b722-29beeb23d053.gif)|![녹화_2023_02_05_02_33_16_321](https://user-images.githubusercontent.com/96832560/216781411-ddf1a7ae-3f66-4d89-a2a7-7926b58fc291.gif)|
| 서버가 run 상태가 되면 각 이용자들은 오목 서버에 접속할 수 있습니다. | 이용자가 방을 생성하면 다른 이용자들은 로비에서 생성된 방 목록을 확인할 수 있습니다. |

| 대결할 참가자 및 관전자들 입장 | 채팅 기능 |
|:----------------------------:|:--------:|
|![녹화_2023_02_05_02_42_29_105](https://user-images.githubusercontent.com/96832560/216781787-33160874-4cb2-45a5-8617-b0e1e9ecc311.gif)|![녹화_2023_02_05_02_45_21_947](https://user-images.githubusercontent.com/96832560/216781910-d2e0dcfd-ebef-421a-b101-6a9d5c5d0519.gif)|
| 방을 생성한 이용자는 대결할 참가자가 입장할 때까지 대기합니다. </br>대결 상대로 참가하지 않고 관전자로도 입장이 가능합니다. (관전자는 돌을 둘 수 없습니다)| 게임 참가자와 관전자들은 채팅 기능을 이용할 수 있습니다. |

| 돌 두기 | 무르기 기능 |
|:------:|:-----------:|
|![녹화_2023_02_05_02_51_55_575](https://user-images.githubusercontent.com/96832560/216782171-26320795-2b28-4974-9d2c-56035f702004.gif)|![녹화_2023_02_05_03_03_36_860](https://user-images.githubusercontent.com/96832560/216782642-3b4fb0c0-5f01-4ac8-aa24-3a337db2090f.gif)|
| 선은 흑이며, 방을 생성한 사람이 흑돌을 둡니다. </br>흑과 백이 번갈아가면서 돌을 하나씩 둘 수 있습니다. | 참가자는 상대방에게 무르기 요청을 보낼 수 있습니다. </br>상대방이 이에 응할 경우 상대방이 가장 마지막에 놓은 돌이 없어집니다. |

| 3-3 | 4-4 |
|:---:|:---:|
|![녹화_2023_02_05_02_58_12_832](https://user-images.githubusercontent.com/96832560/216782452-2f82e8af-7b82-452b-a550-48d5be8d3f31.gif)|![녹화_2023_02_05_03_01_30_451](https://user-images.githubusercontent.com/96832560/216782561-4d9e74f2-f07b-41b2-af2c-71b9ed1dc505.gif)|
| 렌주룰에 따라, 흑만 3-3이 금지됩니다. | 렌주룰에 따라, 흑만 4-4가 금지됩니다.|
> #### 3-3이란?
> **열린 3**(즉, 양 쪽이 막혀있지 않은 상태로 3개의 흑돌이 나란히 나열된 상태)이 2개가 생기는 모양을 뜻합니다. </br>
> 흑이 어떤 자리에 착수할 시에 열린 3이 동시에 2개가 생긴다면, 흑은 해당 자리에 착수할 수 없습니다. </br>
> 단, 두 3중 하나라도 백돌이나 벽에 의해 막혀있을 경우에는 착수할 수 있습니다. 이는 4-4도 마찬가지입니다.

| 장목 | 게임 종료 |
|:--------:|:------------:|
|![녹화_2023_02_05_03_22_06_90](https://user-images.githubusercontent.com/96832560/216783316-959a18a4-6b15-4514-875a-467847a3cc21.gif)|![녹화_2023_02_05_03_10_16_146](https://user-images.githubusercontent.com/96832560/216782878-bc8c1057-6637-4bc9-aa7d-11a089fff09b.gif)|
| 렌주룰에 따라, 흑만 장목이 금지됩니다. | 각 참가자들에게 우승과 패배 안내 메세지가 나오고, 관전자들에게는 우승한 참가자가 누구인지 알려줍니다. |
> #### 장목이란?
> 돌이 6개 이상 나란히 놓여 있는 상태를 말합니다.

| 기권 | 다중 방 생성 |
|:----:|:-----------:|
|![녹화_2023_02_05_03_17_39_590](https://user-images.githubusercontent.com/96832560/216783161-1098b08d-e38c-4a5f-a22d-485a36f360dc.gif)|![녹화_2023_02_05_03_29_26_362](https://user-images.githubusercontent.com/96832560/216783677-b42a3c21-a928-459e-8802-e9c21e0fa864.gif)|
| 게임이 끝나지 않아도 참가자는 도중에 퇴장을 할 수 있으며, 기권으로 처리되어 패배하게 됩니다. | 서버는 여러 방들을 따로 관리할 수 있습니다. </br>한 방에서 보낸 채팅이나 놓은 돌 위치 정보 등의 데이터들은 다른 방에게 영향을 미치지 않습니다. |
</br>

---
</br>

## 개발 기획
- 시스템 구성도
![omok_system_diagram](https://user-images.githubusercontent.com/96832560/217282479-14f30635-e314-4afd-9600-21b404e041e5.png)
  - 서버 thread가 실행되는 동안, Client 객체가 생성되면 해당 유저를 관리하는 UserService Thread가 생성되도록 하였습니다.
  - 유저가 방을 생성하면 Room thread가 생성되어 실행되고, 그 Room thread는 해당 방에 있는 참가자와 관전자들을 관리하도록 하였습니다.

- 시스템 흐름도
![omok_system_flowchart](https://user-images.githubusercontent.com/96832560/218137088-6966ceb5-6418-43b7-99ec-e920e129d9d1.PNG)
  - 서버가 클라이언트로부터 받는 메세지의 코드에 따라 해야 할 동작들에 대해 정리하였습니다. (그 예시는 밑에 [메소드 흐름 예시](#메소드-흐름-예시)에서 서술)
</br>

---
</br>

## 메소드 흐름 예시
- 유저가 로그인을 할 때

ClientLobby.java
```java
...

try {
  socket = new Socket(ip_addr, Integer.parseInt(port_no));
  oos = new ObjectOutputStream(socket.getOutputStream());
  oos.flush();
  ois = new ObjectInputStream(socket.getInputStream());
			
  Msg mg = new Msg(userName, "100", "Hello");
  SendObject(mg);
			
  ListenNetwork net = new ListenNetwork();
  net.start();
} catch (NumberFormatException | IOException e) {
  e.printStackTrace();
}
    
...
```
> 유저는 서버에 연결하면서 서버에 '100'코드를 전송합니다.

</br>

GameServer.java
```java
...

public void Login() {
  AppendText("새로운 참가자 " + UserName + " 입장.");
	AppendText("현재 참가자 수 : " + UserVec.size());
	Msg mg = new Msg("Server", "200", "Sending room list");
	mg.roomTitleList = RoomTitleList;
	WriteOneObject(mg);
}

...

if (mg.code.matches("100")) {
  UserName = mg.userName;
  Login();
}
```

> 서버는 유저에게 '100' 코드를 받고, Login() 메소드를 실행해 유저에게 200코드를 전송하며 방 리스트 정보를 보내줍니다.

</br>

ClientLobby.java
```java
...

case "200": // 로비 화면 refresh
  roomTitleList = mg.roomTitleList;
  roomList.setListData(roomTitleList);
  repaint();
  break;

...
```
> 유저는 서버에게 받은 방 리스트 정보를 토대로 로비 화면에서 방 리스트들을 볼 수 있게 됩니다.

  

</br>

---
</br>

## 개선할 점
- 가끔 퇴장 처리가 잘 되지 않는 버그
- 창 최소화 후 다시 플로팅했을 시 바둑돌 위치가 망가지는 점
- 바둑돌을 둘 수 없는 곳(흑돌의 3-3, 4-4 등)에 X 표식같은 것을 시각적으로 보여주기
- 복기 기능 - 화살표로 넘기면서 단계별로 볼 수 있게
- 전체적인 UI 개선
</br>

---
</br>

## 기타 사항
- 제작 기간
  - 2021/11/1 ~ 2021/12/16
