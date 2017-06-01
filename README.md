# Arduino Bluetooth Bot
An Arduino Bot controlled via Android through Bluetooth Serial Port Profile.    
Based on [**The Node Bot**](https://www.collaborizm.com/project/r1dE09adg) project.

&nbsp;

## High Level Components
### Arduino Mega 2560
- Connects to the HC-05 Bluetooth Module via **Serial1** hardware serial port.
- HC-05 is configured in Serial Port Profile.
- Default device name is **HC-05** and PIN is **1234**.
- 4 individual motors for wheels, but 2 on each side are controlled as one.
- L298N for motor control.

### Android
- Uses [**Android-BluetoothSPPLibrary**](https://github.com/akexorcist/Android-BluetoothSPPLibrary) by [*Akexorcist*](https://github.com/akexorcist) as Bluetooth wrapper.
- Shows connection state and an activity to choose bluetooth device.

