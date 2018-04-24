package rssirecording.rssirecording;

public class UUIDtoID {
    public String trUUID (String s){
        s = ptr(s);
        return s;
    }
    private String ptr(String s) {
        switch (s) {
            case "0x0193bd410x780df142":
                return "A01";
            case "0x1193bd410x770df142":
                return "A02";
            case "0xfb92bd410x680df142":
                return "A03";
            case "0x4993bd410x640df142":
                return "A04";
            case "0x6793bd410x5d0df142":
                return "A05";
            case "0x8193bd410x540df142":
                return "A06";
            case "0xcb93bd410x520df142":
                return "A07";
            case "0x0e94bd410x4e0df142":
                return "A08";
            case "0x1954c8410xbf3af342":
                return "A10";
            case "0x0354c8410xc43ae942":
                return "A14";
            case "0x6564c8410x6239e942":
                return "A15";
            case "0x0454c8410xc43ae942":
                return "A16";
            case "0xf453c8410xbe3af342":
                return "A17";
            case "0xd57bbb410xa89ef042":
                return "A18";
            case "0x4954c8410x473be942":
                return "A19";
            case "0x2f94bd410x3c0df142":
                return "A20";
            default:
                return "notfind";
        }
    }
}
