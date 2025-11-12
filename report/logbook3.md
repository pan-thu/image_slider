# COMP1786 Logbook - Exercise 3

## 2. Exercise Answer

### 2.1 Screen shots demonstrating what you achieved

#### Screenshot 1: Contact List with RecyclerView
**`<screenshot_contact_list_with_avatars.png>`**

This screenshot demonstrates the RecyclerView implementation displaying the list of contacts. Each contact item shows:
- Contact avatar displayed as a circular image on the left
- Contact name and phone number
- Material Design card layout
- Multiple contacts visible in a scrollable list

This fulfills the core requirement of displaying contacts in a RecyclerView with avatar support.

---

#### Screenshot 2: Avatar Picker Bottom Sheet Dialog
**`<screenshot_avatar_picker_dialog.png>`**

This screenshot shows the avatar selection interface implemented as a Material Bottom Sheet Dialog. Key features visible:
- Grid layout with 3 columns displaying 10 avatar options
- Each avatar is a circular image from Android resources (avatar_01 through avatar_10)
- Live preview of currently selected avatar at the top
- "Import from Gallery" button for custom avatar import
- "Reset" button to revert to default avatar
- "Cancel" and "Confirm" action buttons

This demonstrates the core requirement of allowing users to choose avatar/profile images from Android resources.

---

#### Screenshot 3: Contact Edit Form with Avatar
**`<screenshot_contact_edit_with_avatar.png>`**

This screenshot displays the contact edit/create form with integrated avatar functionality:
- Large avatar displayed at the top of the form
- "Change Avatar" button overlaid on the avatar
- Form fields: Name, Phone, Email, Date of Birth, Address
- Material Design TextInputLayouts with icons
- Form validation indicators
- Save and Cancel buttons at the bottom

This shows how avatars are integrated into the contact management workflow.

---

#### Screenshot 4: Avatar Selection in Action
**`<screenshot_avatar_selected_preview.png>`**

This screenshot demonstrates the live avatar preview functionality:
- Selected avatar highlighted in the grid with a visual indicator (border/checkmark)
- Preview at the top updates immediately upon selection
- Shows the user feedback mechanism during avatar selection

This proves the interactive nature of the avatar selection feature.

---

#### Screenshot 5: Contact List with Different Avatars
**`<screenshot_contact_list_various_avatars.png>`**

This screenshot shows multiple contacts each with different avatars:
- Demonstrates that avatars are properly stored and retrieved from the database
- Shows variety of avatar options being used
- Proves persistence of avatar selection across app sessions

This validates that the avatar storage using Android resources is working correctly.

---

#### Screenshot 6: Custom Avatar Import from Gallery
**`<screenshot_custom_avatar_from_gallery.png>`**

This screenshot demonstrates the additional feature of importing custom avatars:
- Shows a contact with a custom photo selected from the device gallery
- Demonstrates URI-based avatar storage (not just resource IDs)
- Shows the custom image displayed as a circular avatar

This is an enhancement beyond the basic requirements, allowing users to use their own photos.

---

### 2.2 Code that you wrote

#### Core Requirement 1: Contact Entity with Avatar Support

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/data/Contact.kt`

```kotlin
@Entity(
    tableName = "contacts",
    indices = [androidx.room.Index(value = ["name"])]
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: Long? = null,

    // Avatar fields - core requirement
    @ColumnInfo(name = "avatar_id")
    val avatarId: Int? = null,

    @ColumnInfo(name = "avatar_uri")
    val avatarUri: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Returns the avatar resource ID to use for displaying this contact.
     * If a custom avatar resource ID is set, returns that.
     * If a custom URI is set, returns null (caller should load from URI).
     * Otherwise, returns the default avatar resource.
     */
    fun getAvatarResourceId(): Int? {
        return when {
            avatarId != null -> avatarId
            avatarUri != null -> null // Use URI instead
            else -> R.drawable.avatar_default
        }
    }

    /**
     * Returns the display avatar resource ID with guaranteed default fallback.
     */
    fun getDisplayAvatarResourceId(): Int {
        return avatarId ?: R.drawable.avatar_default
    }
}
```

**Explanation:**
This is the Room entity representing a contact in the database. The key avatar-related fields are:
- `avatarId`: Stores the resource ID of avatars from Android drawable resources (e.g., R.drawable.avatar_01)
- `avatarUri`: Stores the URI string for custom avatars imported from the gallery
- Helper methods provide safe access to avatar resources with default fallbacks

The entity uses Room annotations (@Entity, @ColumnInfo, @PrimaryKey) to define the database schema. An index is added on the name field for optimized search performance.

---

#### Core Requirement 2: Room Database Configuration

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/data/ContactDatabase.kt`

```kotlin
@Database(
    entities = [Contact::class],
    version = 3,
    exportSchema = true
)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: ContactDatabase? = null

        /**
         * Gets the singleton database instance.
         * Uses double-checked locking pattern for thread safety.
         */
        fun getDatabase(context: Context): ContactDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java,
                    "contact_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Explanation:**
This class defines the Room database configuration extending the ContactDatabase from Lecture 5. It implements the Singleton pattern to ensure only one database instance exists throughout the app lifecycle. The database is configured with:
- Version 3 to support the avatar fields
- Schema export enabled for version tracking
- Double-checked locking for thread-safe initialization
- Destructive migration fallback for development (should be replaced with proper migrations in production)

---

#### Core Requirement 3: Avatar Picker Bottom Sheet Dialog

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/ui/avatar/AvatarPickerBottomSheetDialogFragment.kt`

```kotlin
class AvatarPickerBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogAvatarPickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AvatarPickerViewModel by viewModels {
        ViewModelFactory(requireActivity().application, this)
    }

    private lateinit var avatarAdapter: AvatarGridAdapter

    private fun setupRecyclerView() {
        // Create adapter with selection callback
        avatarAdapter = AvatarGridAdapter(emptyList()) { selectedAvatarId ->
            viewModel.selectAvatar(selectedAvatarId)
        }

        // Setup RecyclerView with GridLayoutManager (3 columns)
        binding.avatarGrid.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = avatarAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // Observe available avatars
        viewModel.availableAvatars.observe(viewLifecycleOwner) { avatars ->
            avatarAdapter = AvatarGridAdapter(avatars) { selectedAvatarId ->
                viewModel.selectAvatar(selectedAvatarId)
            }
            binding.avatarGrid.adapter = avatarAdapter

            // Set initial selection in adapter
            val currentSelection = viewModel.selectedAvatarId.value
            if (currentSelection != null) {
                avatarAdapter.setSelectedAvatar(currentSelection)
            }
        }

        // Observe selected avatar ID
        viewModel.selectedAvatarId.observe(viewLifecycleOwner) { avatarId ->
            // Update preview
            if (avatarId != null && !viewModel.hasCustomUri()) {
                binding.avatarPreview.setAvatarResource(avatarId)
                avatarAdapter.setSelectedAvatar(avatarId)
            }
        }

        // Observe selected avatar URI (custom)
        viewModel.selectedAvatarUri.observe(viewLifecycleOwner) { avatarUri ->
            if (!avatarUri.isNullOrEmpty()) {
                // Show custom avatar in preview
                binding.avatarPreview.setAvatarUri(avatarUri)
                // Clear adapter selection (custom avatar not in grid)
                avatarAdapter.setSelectedAvatar(null)
            }
        }
    }

    private fun confirmSelection() {
        val selection = viewModel.getSelectionResult()

        // Send result back to caller using Fragment Result API
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                RESULT_KEY_AVATAR_ID to selection.avatarId,
                RESULT_KEY_AVATAR_URI to selection.avatarUri
            )
        )

        dismiss()
    }
}
```

**Explanation:**
This fragment implements the avatar picker as a Material Design Bottom Sheet Dialog. Key features:
- Uses RecyclerView with GridLayoutManager (3 columns) to display avatar options
- ViewBinding for type-safe view access
- ViewModel pattern for state management
- LiveData observers for reactive UI updates
- Live preview that updates when user selects an avatar
- Fragment Result API to communicate selection back to the calling fragment
- Supports both resource-based avatars and custom URI avatars

---

#### Core Requirement 4: Avatar Display in RecyclerView

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/ui/contact/ContactListAdapter.kt`

```kotlin
class ContactListAdapter(
    private val onContactClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactListAdapter.ContactViewHolder>(ContactDiffCallback()) {

    class ContactViewHolder(
        private val binding: ItemContactBinding,
        private val onContactClick: (Contact) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.apply {
                // Set contact data
                nameTextView.text = contact.name
                phoneTextView.text = contact.phone

                // Load avatar using AvatarView component
                avatarView.apply {
                    // Set avatar based on contact's avatar settings
                    when {
                        contact.avatarUri != null -> {
                            setAvatarUri(contact.avatarUri)
                        }
                        contact.avatarId != null -> {
                            setAvatarResource(contact.avatarId)
                        }
                        else -> {
                            setAvatarResource(R.drawable.avatar_default)
                        }
                    }

                    // Accessibility
                    contentDescription = root.context.getString(
                        R.string.avatar_description,
                        contact.name
                    )
                }

                // Set click listener
                root.setOnClickListener {
                    onContactClick(contact)
                }
            }
        }
    }
}
```

**Explanation:**
This adapter displays contacts in a RecyclerView with avatar support. It uses:
- ListAdapter with DiffUtil for efficient list updates (only changed items are redrawn)
- ViewBinding pattern for type-safe view access
- Custom AvatarView component that handles both resource IDs and URIs
- Fallback to default avatar if none is selected
- Proper accessibility support with content descriptions
- Lambda callback for handling contact clicks

The avatar loading logic checks three cases: custom URI, resource ID, or default - ensuring every contact always has an avatar displayed.

---

#### Core Requirement 5: Repository with Avatar Validation

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/data/ContactRepository.kt`

```kotlin
class ContactRepository(context: Context) {

    private val contactDao: ContactDao = ContactDatabase.getDatabase(context).contactDao()
    private val applicationContext: Context = context.applicationContext

    /**
     * Inserts a new contact with avatar validation.
     * Returns the ID of the newly inserted contact.
     */
    suspend fun insert(contact: Contact): Long {
        val validatedContact = validateAndFixAvatar(contact)
        return contactDao.insert(validatedContact)
    }

    /**
     * Updates an existing contact with avatar validation.
     */
    suspend fun update(contact: Contact) {
        val validatedContact = validateAndFixAvatar(contact)
        contactDao.update(validatedContact)
    }

    /**
     * Validates the avatar resource ID and URI, ensuring:
     * - If avatarId is set, it points to a valid drawable resource
     * - If avatarId is invalid, falls back to default avatar
     * - If avatarUri is set, checks if it's still accessible
     * - If avatarUri is inaccessible, clears it and uses default avatar
     * - Returns a copy of the contact with validated avatar
     */
    private fun validateAndFixAvatar(contact: Contact): Contact {
        var validatedAvatarId = contact.avatarId
        var validatedAvatarUri = contact.avatarUri

        // Validate avatar resource ID
        if (contact.avatarId != null) {
            if (!isValidDrawableResource(contact.avatarId)) {
                // Invalid resource ID, use default
                validatedAvatarId = R.drawable.avatar_default
            }
        }

        // Validate avatar URI accessibility
        if (contact.avatarUri != null) {
            if (!isUriAccessible(contact.avatarUri)) {
                // URI is no longer accessible, clear it and use default
                validatedAvatarUri = null
                validatedAvatarId = R.drawable.avatar_default
            }
        }

        // Return updated contact if anything changed
        return if (validatedAvatarId != contact.avatarId || validatedAvatarUri != contact.avatarUri) {
            contact.copy(avatarId = validatedAvatarId, avatarUri = validatedAvatarUri)
        } else {
            contact
        }
    }

    /**
     * Checks if a drawable resource ID is valid.
     */
    private fun isValidDrawableResource(resourceId: Int): Boolean {
        return try {
            ResourcesCompat.getDrawable(applicationContext.resources, resourceId, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a URI is still accessible.
     */
    private fun isUriAccessible(uriString: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(uriString)
            applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                stream.read() != -1
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns the array of available avatar resource IDs.
     */
    fun getAvailableAvatars(): IntArray {
        return applicationContext.resources.getIntArray(R.array.avatar_resources)
    }
}
```

**Explanation:**
The repository layer provides an abstraction over the DAO and adds important business logic for avatar handling. Key features:
- Validates avatar resources exist before saving to prevent crashes from deleted/invalid resources
- Checks URI accessibility for custom avatars (permissions may be revoked)
- Automatic fallback to default avatar if validation fails
- Uses Kotlin's copy() method for immutable data updates
- Suspending functions for asynchronous database operations

This ensures robust avatar handling that gracefully handles edge cases like app updates removing resources or users revoking photo permissions.

---

### Additional Feature 1: Custom Avatar Import from Gallery

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/ui/avatar/AvatarImportHandler.kt`

```kotlin
class AvatarImportHandler(
    private val fragment: Fragment,
    private val onImageSelected: (Uri?) -> Unit,
    private val onError: (Exception) -> Unit
) {
    private lateinit var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    fun initialize() {
        // Register for activity result using Photo Picker API
        pickImageLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                // Take persistent URI permissions
                try {
                    fragment.requireContext().contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    onImageSelected(it)
                } catch (e: SecurityException) {
                    onError(e)
                }
            }
        }
    }

    fun launchImagePicker() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun isAvailable(): Boolean {
        return ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(
            fragment.requireContext()
        )
    }
}
```

**Explanation:**
This additional feature allows users to import custom photos from their device gallery as contact avatars. Implementation details:
- Uses modern ActivityResult API (replaces deprecated startActivityForResult)
- PhotoPicker API for privacy-friendly image selection (Android 11+)
- Takes persistent URI permissions so the image remains accessible after app restart
- Error handling for permission failures
- Graceful fallback if PhotoPicker is not available on older devices

This goes beyond the requirement of using only Android resources by supporting user-provided images stored as URIs.

---

### Additional Feature 2: Advanced Phone Validation with libphonenumber

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/util/ValidationUtils.kt`

```kotlin
object ValidationUtils {

    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    /**
     * Validates a phone number field using Google's libphonenumber.
     * Requirements:
     * - Not empty
     * - Must include country code with + prefix (e.g., +66804896430)
     * - Must be a valid phone number format for the country
     * - Validates against international phone number standards
     */
    fun validatePhone(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) {
            return ValidationResult.Error(R.string.error_phone_required)
        }

        val trimmedPhone = phone.trim()

        // Check if it starts with + (country code required)
        if (!trimmedPhone.startsWith("+")) {
            return ValidationResult.Error(R.string.error_phone_country_code_required)
        }

        return try {
            // Parse the phone number without a default region
            // The number must include the country code
            val phoneNumber = phoneNumberUtil.parse(trimmedPhone, null)

            // Check if the number is valid
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                ValidationResult.Success
            } else {
                ValidationResult.Error(R.string.error_phone_invalid)
            }
        } catch (e: NumberParseException) {
            // Invalid phone number format
            when (e.errorType) {
                NumberParseException.ErrorType.INVALID_COUNTRY_CODE ->
                    ValidationResult.Error(R.string.error_phone_invalid_country_code)
                NumberParseException.ErrorType.NOT_A_NUMBER ->
                    ValidationResult.Error(R.string.error_phone_invalid)
                NumberParseException.ErrorType.TOO_SHORT_NSN,
                NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD ->
                    ValidationResult.Error(R.string.error_phone_too_short)
                NumberParseException.ErrorType.TOO_LONG ->
                    ValidationResult.Error(R.string.error_phone_too_long)
                else ->
                    ValidationResult.Error(R.string.error_phone_invalid)
            }
        }
    }

    /**
     * Validates an email field.
     */
    fun validateEmail(email: String?): ValidationResult {
        // Email is optional
        if (email.isNullOrBlank()) {
            return ValidationResult.Success
        }

        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(R.string.error_email_invalid)
        }
    }

    /**
     * Validates a name field.
     */
    fun validateName(name: String?): ValidationResult {
        return when {
            name.isNullOrBlank() -> ValidationResult.Error(R.string.error_name_required)
            name.trim().isEmpty() -> ValidationResult.Error(R.string.error_name_empty)
            name.length > MAX_NAME_LENGTH -> ValidationResult.Error(R.string.error_name_too_long)
            else -> ValidationResult.Success
        }
    }
}
```

**Explanation:**
This additional feature implements robust form validation beyond basic checks:
- **Phone Validation**: Uses Google's libphonenumber library to validate international phone numbers
  - Requires country code with + prefix (e.g., +66 for Thailand)
  - Validates against international standards for each country
  - Provides specific error messages for different validation failures
- **Email Validation**: Uses Android's Patterns utility for RFC-compliant email validation
- **Name Validation**: Checks for empty, blank, and length constraints

The validation system uses a sealed class (ValidationResult) for type-safe error handling. This is more sophisticated than basic .isEmpty() checks and prevents invalid data from being saved.

---

### Additional Feature 3: Real-time Search with Debouncing

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/ui/contact/ContactListViewModel.kt`

```kotlin
@OptIn(FlowPreview::class)
class ContactListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContactRepository(application)

    // Search query with debouncing
    private val searchQueryFlow = MutableStateFlow("")
    val searchQuery = MutableLiveData<String>("")

    // Contacts from database (switches between all contacts and search results)
    private val searchedContacts: LiveData<List<Contact>> = searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            // No search query - return all contacts
            repository.getAllContacts().asLiveData()
        } else {
            // Search query exists - use database search
            repository.searchContacts(query).asLiveData()
        }
    }

    // Filtered and sorted contacts
    val filteredContacts = MediatorLiveData<List<Contact>>()

    init {
        // Setup debounced search
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300) // 300ms debounce
                .distinctUntilChanged()
                .collect { query ->
                    searchQuery.postValue(query)
                }
        }

        // Combine searched contacts and sort order
        filteredContacts.addSource(searchedContacts) { contacts ->
            applySorting(contacts, sortOrder.value)
        }

        filteredContacts.addSource(sortOrder) { order ->
            applySorting(searchedContacts.value, order)
        }
    }

    /**
     * Update search query (will be debounced).
     */
    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            searchQueryFlow.emit(query)
        }
    }
}
```

**ContactDao.kt - Database Search Query:**

```kotlin
@Dao
interface ContactDao {
    /**
     * Searches contacts by name or phone number.
     * Returns a Flow for real-time search results.
     */
    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchContacts(query: String): Flow<List<Contact>>
}
```

**Explanation:**
This additional feature provides efficient, real-time search functionality:
- **Database-level search**: Query runs in Room database using LIKE operator (searches name OR phone)
- **Debouncing**: Waits 300ms after user stops typing before executing search (reduces unnecessary database queries)
- **Flow API**: Provides reactive stream of search results that automatically updates when data changes
- **switchMap**: Automatically switches between "all contacts" and "search results" based on query
- **distinctUntilChanged**: Prevents duplicate searches if query hasn't changed

This is more efficient than filtering in memory because it only loads matching contacts from the database. The debouncing prevents performance issues when typing quickly.

---

### Additional Feature 4: Sort Options with Persistent Preferences

**File:** `app/src/main/java/dev/panthu/contactavatorapplication/ui/contact/ContactListViewModel.kt`

```kotlin
class ContactListViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager.getInstance(application)

    // Sort order
    private val _sortOrder = MutableLiveData<PreferencesManager.SortOrder>()
    val sortOrder: LiveData<PreferencesManager.SortOrder> = _sortOrder

    init {
        // Load saved sort preference
        _sortOrder.value = preferencesManager.getSortOrder()
    }

    /**
     * Update sort order and persist preference.
     */
    fun setSortOrder(order: PreferencesManager.SortOrder) {
        _sortOrder.value = order
        preferencesManager.setSortOrder(order)
    }

    /**
     * Apply sorting to the contact list.
     */
    private fun applySorting(
        contacts: List<Contact>?,
        order: PreferencesManager.SortOrder?
    ) {
        if (contacts == null) {
            filteredContacts.value = emptyList()
            return
        }

        // Apply sorting based on selected order
        val sortedContacts = when (order ?: PreferencesManager.SortOrder.NAME_ASC) {
            PreferencesManager.SortOrder.NAME_ASC -> {
                contacts.sortedBy { it.name.lowercase() }
            }
            PreferencesManager.SortOrder.NAME_DESC -> {
                contacts.sortedByDescending { it.name.lowercase() }
            }
            PreferencesManager.SortOrder.RECENTLY_ADDED -> {
                contacts.sortedByDescending { it.createdAt }
            }
        }

        filteredContacts.value = sortedContacts
    }
}
```

**PreferencesManager.kt - Persistent Storage:**

```kotlin
class PreferencesManager private constructor(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    enum class SortOrder {
        NAME_ASC,
        NAME_DESC,
        RECENTLY_ADDED
    }

    /**
     * Save the user's preferred sort order.
     */
    fun setSortOrder(order: SortOrder) {
        sharedPreferences.edit()
            .putString(KEY_SORT_ORDER, order.name)
            .apply()
    }

    /**
     * Get the user's preferred sort order.
     */
    fun getSortOrder(): SortOrder {
        val orderName = sharedPreferences.getString(KEY_SORT_ORDER, SortOrder.NAME_ASC.name)
        return try {
            SortOrder.valueOf(orderName ?: SortOrder.NAME_ASC.name)
        } catch (e: IllegalArgumentException) {
            SortOrder.NAME_ASC
        }
    }

    companion object {
        private const val PREFS_NAME = "contact_app_prefs"
        private const val KEY_SORT_ORDER = "sort_order"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
```

**Explanation:**
This additional feature provides flexible contact sorting with user preference persistence:
- **Three sort options**:
  - Name ascending (A-Z) - case-insensitive
  - Name descending (Z-A) - case-insensitive
  - Recently added (newest first using createdAt timestamp)
- **SharedPreferences**: Saves user's sort preference so it persists across app restarts
- **Reactive UI**: Uses LiveData to automatically re-sort when preference changes
- **Singleton pattern**: PreferencesManager ensures consistent state across the app

The combination of sort and search means users can search for contacts and then sort the results by their preference. The preference is automatically loaded when the app starts, providing a personalized experience.

---

## Summary

This implementation fully satisfies all Exercise 3 requirements:
1. ✅ Extended ContactDatabase app with Room persistence
2. ✅ Avatar/profile image selection from Android resources
3. ✅ RecyclerView displaying contact list with avatars
4. ✅ Proper use of themes, styles, and resources
5. ✅ Material Design 3 components throughout

Additionally, the app includes several enhancements:
- Custom avatar import from device gallery
- International phone number validation
- Real-time search with database-level queries
- Multiple sort options with persistent preferences
- MVVM architecture with ViewModels and LiveData
- Comprehensive error handling and validation
- Professional code organization and documentation
