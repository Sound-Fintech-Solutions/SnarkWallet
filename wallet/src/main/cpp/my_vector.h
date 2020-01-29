//
// Created by Dmitry on 5/23/2019.
//


#ifndef MY_VECTOR_H
#define MY_VECTOR_H

using std::string;

class my_vector
{
public:
    int32_t * data;
    int allocated; //����� ������, ���������� ��� ������

    my_vector();
    my_vector(int);
    my_vector(my_vector const & other);
    ~my_vector();

    my_vector& operator ++ ();
    my_vector& operator ++ (int);
    my_vector& operator -- ();
    my_vector& operator -- (int);

    my_vector& operator = (my_vector const & other);

    my_vector& operator + ();
    my_vector operator - () const;
    my_vector& operator ~ ();

    my_vector& operator += (my_vector const & other);
    my_vector& operator -= (my_vector const & other);
    my_vector& operator *= (my_vector const & other);
    my_vector& operator /= (my_vector const & other);
    my_vector& operator %= (my_vector const & other);

    my_vector& operator &= (my_vector const & other);
    my_vector& operator |= (my_vector const & other);
    my_vector& operator ^= (my_vector const & other);

    my_vector& operator >>= (int other);
    my_vector& operator <<= (int other);

    my_vector& operator ! ();

    void fitSize(my_vector & other);
    void reallocate(int new_allocate);
    void shrink_to_fit();
    void divide(my_vector const& other, my_vector& res, my_vector& rest);
    bool neg() const;
    bool pos() const;

    friend bool operator < (my_vector &, my_vector &);
    friend bool operator == (my_vector const &, my_vector const &);
    friend std::ostream& operator << (std::ostream &, my_vector const &);
private:

};

my_vector operator + (my_vector const & a, my_vector const & b);
my_vector operator - (my_vector const & a, my_vector const& b);
my_vector operator * (my_vector const & a, my_vector const& b);
my_vector operator / (my_vector const & a, my_vector const& b);
my_vector operator % (my_vector const & a, my_vector const& b);

my_vector operator & (my_vector const & a, my_vector const& b);
my_vector operator | (my_vector const & a, my_vector const& b);
my_vector operator ^ (my_vector const & a, my_vector const& b);

my_vector operator >> (my_vector & a, int b);
my_vector operator << (my_vector & a, int b); // TODO: const

bool operator < (my_vector  & a, my_vector & b); // TODO: const
bool operator > (my_vector  & a, my_vector & b);
bool operator <= (my_vector & a, my_vector & b);
bool operator >= (my_vector & a, my_vector & b);
bool operator == (my_vector const & a, my_vector const & b);
bool operator != (my_vector & a, my_vector & b);
#endif

