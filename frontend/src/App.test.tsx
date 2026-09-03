import { fireEvent, render, screen, within } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('ResolveHub dashboard', () => {
  it('filters tickets by status', () => {
    render(<App />)

    fireEvent.click(screen.getByRole('button', { name: 'Resolved' }))

    const table = screen.getByRole('table')
    expect(within(table).getByText('Webhook retry policy question')).toBeInTheDocument()
    expect(within(table).queryByText('Payment reconciliation is delayed')).not.toBeInTheDocument()
  })

  it('creates a ticket from the dashboard', () => {
    render(<App />)

    fireEvent.click(screen.getByRole('button', { name: /new ticket/i }))
    fireEvent.change(screen.getByLabelText('Ticket title'), { target: { value: 'Production report unavailable' } })
    fireEvent.change(screen.getByLabelText('Customer'), { target: { value: 'Acme Data' } })
    fireEvent.click(screen.getByRole('button', { name: 'Create ticket' }))

    const table = screen.getByRole('table')
    expect(within(table).getByText('Production report unavailable')).toBeInTheDocument()
    expect(within(table).getByText(/RH-1049/)).toBeInTheDocument()
  })
})
