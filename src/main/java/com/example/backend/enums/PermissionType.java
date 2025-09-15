package com.example.backend.enums;

public enum PermissionType {
    // Quyền xem ví
    VIEW_WALLET("Xem thông tin ví"),
    VIEW_BALANCE("Xem số dư"),
    VIEW_TRANSACTIONS("Xem giao dịch"),
    
    // Quyền chỉnh sửa ví
    EDIT_WALLET("Chỉnh sửa thông tin ví"),
    ADD_TRANSACTION("Thêm giao dịch"),
    EDIT_TRANSACTION("Chỉnh sửa giao dịch"),
    DELETE_TRANSACTION("Xóa giao dịch"),
    
    // Quyền quản trị
    MANAGE_PERMISSIONS("Quản lý quyền truy cập"),
    SHARE_WALLET("Chia sẻ ví với người khác"),
    DELETE_WALLET("Xóa ví"),
    TRANSFER_OWNERSHIP("Chuyển quyền sở hữu"),
    
    // Quyền báo cáo
    VIEW_REPORTS("Xem báo cáo"),
    EXPORT_DATA("Xuất dữ liệu");

    private final String displayName;

    PermissionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
