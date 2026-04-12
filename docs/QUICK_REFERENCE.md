# Hope-Finder V2 - Quick Reference Guide

## 🎨 New Color Scheme At a Glance

### Primary Colors
```
Deep Blue (Primary):      #1e40af     ← Headers, important elements
Cyan Accent (Main):       #06b6d4     ← Radar, key interactions
Deep Navy (Background):   #0f172a     ← Page backgrounds
Slate (Surface):          #1e293b     ← Cards, containers
```

### Alert & Status Colors
```
Critical Red:             #ef4444     ← Emergency alerts
Warning Amber:            #f59e0b     ← Caution/medium priority
Success Emerald:          #10b981     ← Positive actions
Info Blue:                #3b82f6     ← Information
```

### Text Colors
```
Primary (White):          #FFFFFF     ← Main text
Secondary (Light Gray):   #cbd5e1     ← Supporting text
Tertiary (Gray):          #94a3b8     ← Hints, disabled
```

---

## 🔄 Quick Migration

### Old → New Color Mappings
```kotlin
// OLD CODE - Don't use anymore
Color(0xFFFF6B00)           → AccentCyan               // Orange → Cyan
DarkBackground              → BackgroundDeepNavy       // Automatic via migration
Color.White                 → TextPrimaryWhite         // Use semantic name
SignalStrengthWeak         → StatusCriticalRed        // More semantic
SuccessGreen               → StatusSuccessEmerald     // More professional
```

### Using New Colors
```kotlin
// NEW: Import from theme
import com.tryout.hopefinder.ui.theme.*

// NEW: Use in Composables
Box(
    modifier = Modifier
        .background(SurfaceSlate, RoundedCornerShape(8.dp))
        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
)

Text(
    "Status: Active",
    color = TextPrimaryWhite,
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium
)
```

---

## 📝 Typography Quick Reference

### Using Typography
```kotlin
// Built-in Material 3 typography
Text("Page Title", style = MaterialTheme.typography.displayLarge)
Text("Section", style = MaterialTheme.typography.headlineLarge)
Text("Body text", style = MaterialTheme.typography.bodyMedium)
Text("Button", style = MaterialTheme.typography.labelLarge)
```

### Font Sizes
```
Display:   32sp → 24sp (Titles)
Headline:  22sp → 18sp (Headers)
Title:     16sp → 12sp (Labels)
Body:      16sp → 12sp (Content)
Label:     14sp → 11sp (Controls)
```

---

## 🎯 Component Usage Examples

### Status Badge
```kotlin
// Shows if something is connected/active
StatusBadge(
    label = "Wi-Fi",
    isActive = deviceStatus.wifiConnected  // Green when true
)
```

### Motion Level Indicator  
```kotlin
// Shows detection urgency
MotionLevelIndicator(
    motionLevel = "HIGH",
    confidence = 85  // Color changes: green → orange → red
)
```

### Alert Card
```kotlin
// Display a detection alert
AlertCard(
    confidence = 90,
    sector = "N",
    angle = 0,
    timestamp = System.currentTimeMillis(),
    onDismiss = { viewModel.dismissAlert() }
    // Border auto-colors by severity
)
```

### Emergency Button
```kotlin
// Large action button
EmergencyButton(
    text = "Start Scan",
    onClick = { viewModel.startScan() },
    isDestructive = false,  // Cyan accent
    isLoading = false,
    enabled = true
)
```

---

## 🎨 Screen Color Quick Ref

### Dashboard
- **Header**: Primary Blue
- **Icons**: Cyan accent
- **Alerts**: Red borders
- **Success**: Green badges

### Radar
- **Sweep**: Cyan (#06b6d4)
- **Rings**: Cyan with transparency
- **Background**: Deep Navy
- **Text**: White & Gray

### Alerts
- **Critical**: Red (#ef4444)
- **High**: Orange (#f97316)
- **Medium**: Amber (#f59e0b)
- **Empty State**: Green checkmark

### Reports
- **Headers**: Primary Blue
- **Stats**: Cyan & Amber
- **Charts**: Colored by severity
- **Buttons**: Cyan, Blue, Green

---

## 🔧 Common Customizations

### Create Custom Status Card
```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
    colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
    shape = RoundedCornerShape(12.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Title", color = TextPrimaryWhite)
        Text("Subtitle", color = TextSecondaryGray)
    }
}
```

### Create Alert Badge
```kotlin
Box(
    modifier = Modifier
        .size(40.dp)
        .background(StatusCriticalRed.copy(alpha = 0.2f), CircleShape)
        .border(1.dp, StatusCriticalRed, CircleShape),
    contentAlignment = Alignment.Center
) {
    Text(
        count.toString(),
        color = StatusCriticalRed,
        fontWeight = FontWeight.Bold
    )
}
```

---

## 🚀 Performance Tips

1. **Use Color Constants**: Don't create Color() repeatedly
2. **Apply Background First**: Improves rendering performance
3. **Use RoundedCornerShape(8.dp)**: Standard size, cached well
4. **Cache Typography**: Use MaterialTheme.typography
5. **Minimize Gradients**: They're expensive on low-end devices

---

## 🔍 Troubleshooting

### Colors look wrong?
1. Check imports: `import com.tryout.hopefinder.ui.theme.*`
2. Rebuild project: `./gradlew clean build`
3. Clear emulator cache: Extended controls → Wipe data

### Text too large/small?
1. Use predefined `MaterialTheme.typography` styles
2. Check `HopeFinderTypography` in Theme.kt
3. Verify font sizes (sp units, not dp)

### Components not showing?
1. Ensure `HopeFinderTheme` wraps content
2. Check `@Composable` annotations
3. Verify modifier parameters

---

## 📊 Color Accessibility

All colors tested for:
- ✅ Minimum contrast 4.5:1 for AA compliance
- ✅ WCAG 21 standards
- ✅ Colorblind-friendly combinations
- ✅ Dark theme optimization

---

## 🎓 Design System Location

- **Colors**: `app/src/main/java/com/tryout/hopefinder/ui/theme/Color.kt`
- **Typography**: `app/src/main/java/com/tryout/hopefinder/ui/theme/Theme.kt`
- **Shapes**: `app/src/main/java/com/tryout/hopefinder/ui/theme/Theme.kt`
- **Components**: `app/src/main/java/com/tryout/hopefinder/ui/components/Components.kt`
- **Documentation**: `docs/DESIGN_GUIDE.md`

---

## 📞 Quick Tips

1. **Always use theme colors**: They adapt if theming changes
2. **Test on multiple devices**: Colors may vary by screen
3. **Use semantic names**: More maintainable than hex codes
4. **Group related elements**: Easier to update styling
5. **Document custom changes**: Help future developers

---

## ✅ Version Info

- **Design System**: 2.0
- **Material Design**: 3
- **Color Palette**: Professional Emergency App Style
- **Status**: Production Ready
- **Last Updated**: April 2026

---

**Need more details?** See `docs/DESIGN_GUIDE.md`
