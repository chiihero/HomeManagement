export default {
  reminder: {
    title: 'Reminder Management',
    add: 'Add Reminder',
    edit: 'Edit Reminder',
    delete: 'Delete Reminder',
    complete: 'Complete Reminder',
    search: {
      itemName: 'Item Name',
      type: 'Reminder Type',
      status: 'Status',
      dateRange: 'Reminder Date',
      placeholder: {
        itemName: 'Please enter item name',
        type: 'Please select reminder type',
        status: 'Please select status',
        dateRange: 'Please select date range'
      }
    },
    table: {
      itemName: 'Item Name',
      type: 'Reminder Type',
      reminderDate: 'Reminder Date',
      status: 'Status',
      content: 'Content',
      notificationMethods: 'Notification Methods',
      actions: 'Actions'
    },
    form: {
      item: 'Item',
      type: 'Reminder Type',
      reminderDate: 'Reminder Date',
      content: 'Content',
      notificationMethods: 'Notification Methods',
      daysInAdvance: 'Days in Advance',
      isRecurring: 'Recurring',
      recurringCycle: 'Recurring Cycle',
      placeholder: {
        item: 'Please select item',
        type: 'Please select reminder type',
        reminderDate: 'Please select reminder date',
        content: 'Please enter content',
        recurringCycle: 'Please select recurring cycle'
      }
    },
    types: {
      EXPIRATION: 'Expiration',
      MAINTENANCE: 'Maintenance',
      RETURN: 'Return',
      OTHER: 'Other'
    },
    status: {
      PENDING: 'Pending',
      NOTIFIED: 'Notified',
      COMPLETED: 'Completed',
      EXPIRED: 'Expired'
    },
    notificationMethods: {
      EMAIL: 'Email',
      SYSTEM: 'System',
      SMS: 'SMS'
    },
    recurringCycles: {
      DAILY: 'Daily',
      WEEKLY: 'Weekly',
      MONTHLY: 'Monthly',
      YEARLY: 'Yearly'
    },
    messages: {
      addSuccess: 'Add reminder successfully',
      addFailed: 'Failed to add reminder',
      editSuccess: 'Update reminder successfully',
      editFailed: 'Failed to update reminder',
      deleteSuccess: 'Delete reminder successfully',
      deleteFailed: 'Failed to delete reminder',
      completeSuccess: 'Mark reminder as completed',
      completeFailed: 'Failed to mark reminder',
      confirmDelete: 'Are you sure to delete this reminder?'
    }
  }
} 